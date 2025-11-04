package edu.metrostate.ics372_phase2;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONParser {

    private static final Logger logger = Logger.getLogger(JSONParser.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static Order readOrderFromResourceFolder(String fileName) {
        String Path = "edu/metrostate/ProjectPhase2/Incoming Orders/" + fileName;

        try (InputStream inputStream = JSONParser.class.getClassLoader().getResourceAsStream(Path)) {
            if (inputStream == null) {
                logger.warning("File not found: " + Path);
                return null;
            }

            JsonNode root = MAPPER.readTree(inputStream);
            JsonNode orderNode = root.get("order");
            if (orderNode == null) {
                logger.warning("Missing 'order' field in JSON");
                return null;
            }

            String typeString = orderNode.get("type").asText();
            long orderDateMillis = orderNode.get("order_date").asLong();

            OrderType orderType = OrderType.valueOf(typeString.toUpperCase());
            LocalDateTime orderTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(orderDateMillis), ZoneOffset.UTC);

            List<OrderItem> items = new ArrayList<>();
            JsonNode itemsArray = orderNode.get("items");

            if (itemsArray != null && itemsArray.isArray()) {
                for (JsonNode itemNode : itemsArray) {
                    String name = itemNode.get("name").asText();
                    int quantity = itemNode.get("quantity").asInt();
                    double price = itemNode.get("price").asDouble();

                    items.add(new OrderItem(name, quantity, price));
                }
            }

            return new Order(null, orderTime, items, orderType);

        } catch (JsonParseException jpe) {
            logger.log(Level.SEVERE, "JSON parsing error for file: " + Path, jpe);
            return null;
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Error loading order from resource: " + Path, exception);
            return null;
        }
    }

    /**
     * Reads an order from an external file (not from resources).
     * Used for file system directory reading.
     */
    public static Order readOrderFromExternalFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.warning("External file not found: " + filePath);
                return null;
            }

            JsonNode root = MAPPER.readTree(file);
            JsonNode orderNode = root.get("order");
            if (orderNode == null) {
                logger.warning("Missing 'order' field in JSON: " + filePath);
                return null;
            }

            String typeString = orderNode.get("type").asText();
            long orderDateMillis = orderNode.get("order_date").asLong();

            OrderType orderType = OrderType.valueOf(typeString.toUpperCase());
            LocalDateTime orderTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(orderDateMillis), ZoneOffset.UTC);

            List<OrderItem> items = new ArrayList<>();
            JsonNode itemsArray = orderNode.get("items");

            if (itemsArray != null && itemsArray.isArray()) {
                for (JsonNode itemNode : itemsArray) {
                    String name = itemNode.get("name").asText();
                    int quantity = itemNode.get("quantity").asInt();
                    double price = itemNode.get("price").asDouble();

                    items.add(new OrderItem(name, quantity, price));
                }
            }

            Order order = new Order(null, orderTime, items, orderType);
            order.setSource(file.getName()); // Set source to filename
            return order;

        } catch (JsonParseException jpe) {
            logger.log(Level.SEVERE, "JSON parsing error for external file: " + filePath, jpe);
            return null;
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Error loading order from external file: " + filePath, exception);
            return null;
        }
    }

    public static void exportAllOrders(Collection<Order> orders, String filename) throws IOException {

        if (!filename.toLowerCase().endsWith(".json")) {
            filename += ".json";
        }

        File dir = new File("ICS372_Group3_ProjectPhase2/Exported Orders");
        if (!dir.exists()) dir.mkdirs();

        File outputFile = new File(dir, filename);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // For LocalDateTime support
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, orders);
    }
}
