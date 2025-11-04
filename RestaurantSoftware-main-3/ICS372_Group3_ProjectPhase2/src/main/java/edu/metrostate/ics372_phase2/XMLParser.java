package edu.metrostate.ics372_phase2;



import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for parsing and exporting orders in XML format.
 * Reads orders from XML files in the "incoming-orders" folder and converts them into Order objects.
 * Supports exporting a collection of orders to a formatted XML file.
 */
public class XMLParser {

    private static final Logger logger = Logger.getLogger(XMLParser.class.getName());

    public static Order readOrderFromFolder(String fileName) {
        String resourcePath = "edu/metrostate/ProjectPhase2/Incoming Orders/" + fileName;

        try (InputStream inputStream = XMLParser.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                logger.warning("File not found: " + resourcePath);
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // Get first Order element
            NodeList orderNodes = doc.getElementsByTagName("Order");
            if (orderNodes.getLength() == 0) {
                logger.warning("No <Order> element found in XML.");
                return null;
            }

            Element orderElement = (Element) orderNodes.item(0);
            String orderId = orderElement.getAttribute("id");

            String typeText = orderElement.getElementsByTagName("OrderType").item(0).getTextContent();
            OrderType orderType;
            try {
                orderType = OrderType.valueOf(typeText.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown order type: " + typeText + ", defaulting to TOGO");
                orderType = OrderType.TOGO;
            }

            LocalDateTime orderTime = LocalDateTime.now(ZoneOffset.UTC); // Assign current time

            List<OrderItem> items = new ArrayList<>();
            NodeList itemNodes = orderElement.getElementsByTagName("Item");

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) node;
                    String name = itemElement.getAttribute("type");
                    double price = Double.parseDouble(itemElement.getElementsByTagName("Price").item(0).getTextContent());
                    int quantity = Integer.parseInt(itemElement.getElementsByTagName("Quantity").item(0).getTextContent());

                    items.add(new OrderItem(name, quantity, price));
                }
            }

            return new Order(orderId, orderTime, items, orderType);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading order from resource: " + resourcePath, e);
            return null;
        }
    }

    // Add method with same signature as JSONParser for consistency
    public static Order readOrderFromResourceFolder(String fileName) {
        return readOrderFromFolder(fileName);
    } //change to name of company

    /**
     * Reads an order from an external file (not from resources).
     * Used for directory watching and external file imports.
     */
    public static Order readOrderFromExternalFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.warning("External file not found: " + filePath);
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Enable better error handling for buggy XML
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Custom error handler to continue processing despite errors
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException exception) {
                    logger.warning("XML Warning in " + filePath + ": " + exception.getMessage());
                }
                
                @Override
                public void error(org.xml.sax.SAXParseException exception) {
                    logger.warning("XML Error in " + filePath + ": " + exception.getMessage());
                }
                
                @Override
                public void fatalError(org.xml.sax.SAXParseException exception) {
                    logger.severe("XML Fatal Error in " + filePath + ": " + exception.getMessage());
                }
            });
            
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            // Get first Order element
            NodeList orderNodes = doc.getElementsByTagName("Order");
            if (orderNodes.getLength() == 0) {
                logger.warning("No <Order> element found in XML: " + filePath);
                return null;
            }

            Element orderElement = (Element) orderNodes.item(0);
            String orderId = orderElement.getAttribute("id");
            
            // Handle missing or empty order ID
            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = "UNKNOWN-" + System.currentTimeMillis();
                logger.warning("Missing order ID in " + filePath + ", generated: " + orderId);
            }

            // Handle order type with error recovery
            String typeText = "TOGO"; // default
            NodeList typeNodes = orderElement.getElementsByTagName("OrderType");
            if (typeNodes.getLength() > 0) {
                typeText = typeNodes.item(0).getTextContent();
            }
            
            OrderType orderType;
            try {
                orderType = OrderType.valueOf(typeText.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown order type: " + typeText + " in " + filePath + ", defaulting to TOGO");
                orderType = OrderType.TOGO;
            }

            LocalDateTime orderTime = LocalDateTime.now(ZoneOffset.UTC);

            List<OrderItem> items = new ArrayList<>();
            NodeList itemNodes = orderElement.getElementsByTagName("Item");

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) node;
                    
                    try {
                        String name = itemElement.getAttribute("type");
                        if (name == null || name.trim().isEmpty()) {
                            name = "Unknown Item";
                            logger.warning("Missing item name in " + filePath + ", using default");
                        }
                        
                        double price = 0.0;
                        int quantity = 1;
                        
                        // Safe price parsing
                        NodeList priceNodes = itemElement.getElementsByTagName("Price");
                        if (priceNodes.getLength() > 0) {
                            try {
                                String priceText = priceNodes.item(0).getTextContent().trim();
                                // Remove any currency symbols or units
                                priceText = priceText.replaceAll("[^0-9.]", "");
                                price = Double.parseDouble(priceText);
                            } catch (NumberFormatException e) {
                                logger.warning("Invalid price in " + filePath + " for item " + name + ", using 0.0");
                            }
                        }
                        
                        // Safe quantity parsing
                        NodeList quantityNodes = itemElement.getElementsByTagName("Quantity");
                        if (quantityNodes.getLength() > 0) {
                            try {
                                quantity = Integer.parseInt(quantityNodes.item(0).getTextContent().trim());
                            } catch (NumberFormatException e) {
                                logger.warning("Invalid quantity in " + filePath + " for item " + name + ", using 1");
                            }
                        }

                        items.add(new OrderItem(name, quantity, price));
                        
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing item in " + filePath + ", skipping", e);
                    }
                }
            }
            
            // Ensure we have at least one item
            if (items.isEmpty()) {
                items.add(new OrderItem("Default Item", 1, 0.0));
                logger.warning("No valid items found in " + filePath + ", added default item");
            }

            Order order = new Order(orderId, orderTime, items, orderType);
            order.setSource(file.getName()); // Set source to filename
            return order;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading order from external file: " + filePath, e);
            return null;
        }
    }

    public static void exportAllOrders(Collection<Order> orders, String filename) throws IOException {
        if (!filename.toLowerCase().endsWith(".xml")) {
            filename += ".xml";
        }

        File dir = new File("ICS372_Group3_ProjectPhase2/Exported Orders");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create export directory: " + dir.getAbsolutePath());
        }

        File outputFile = new File(dir, filename);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<Orders>\n");

        for (Order order : orders) {
            sb.append(String.format("  <Order id=\"%s\">\n", order.getOrderId()));
            sb.append(String.format("    <OrderType>%s</OrderType>\n", order.getOrderType().name()));
            for (OrderItem item : order.getItems()) {
                sb.append(String.format("    <Item type=\"%s\">\n", item.name()));
                sb.append(String.format("      <Price>%.2f</Price>\n", item.price()));
                sb.append(String.format("      <Quantity>%d</Quantity>\n", item.quantity()));
                sb.append("    </Item>\n");
            }
            sb.append("  </Order>\n");
        }

        sb.append("</Orders>");

        java.nio.file.Files.writeString(outputFile.toPath(), sb.toString());
    }
}
