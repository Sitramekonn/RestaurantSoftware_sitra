package edu.metrostate.ics372_phase2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles persistence of application state including orders and their statuses.
 * Saves state to a JSON file and restores it on application startup.
 */
public class StateManager {
    
    private static final Logger logger = Logger.getLogger(StateManager.class.getName());
    private static final String STATE_DIR = "ICS372_Group3_ProjectPhase2/AppState";
    private static final String STATE_FILE = "orders_state.json";
    
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    /**
     * Saves the current state of all orders to a persistent file.
     */
    public static void saveState(List<Order> orders) {
        try {
            File dir = new File(STATE_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                logger.severe("Failed to create state directory: " + dir.getAbsolutePath());
                return;
            }
            
            File stateFile = new File(dir, STATE_FILE);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(stateFile, orders);
            logger.info("State saved successfully with " + orders.size() + " orders");
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save application state", e);
        }
    }
    
    /**
     * Loads the previously saved state from the persistent file.
     * Returns an empty list if no saved state exists or if loading fails.
     */
    public static List<Order> loadState() {
        try {
            File stateFile = new File(STATE_DIR, STATE_FILE);
            if (!stateFile.exists()) {
                logger.info("No saved state found, starting with empty order list");
                return new ArrayList<>();
            }
            
            Order[] orders = MAPPER.readValue(stateFile, Order[].class);
            List<Order> orderList = new ArrayList<>();
            for (Order order : orders) {
                orderList.add(order);
            }
            
            logger.info("State loaded successfully with " + orderList.size() + " orders");
            return orderList;
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load application state, starting fresh", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Clears the saved state file.
     */
    public static void clearState() {
        try {
            File stateFile = new File(STATE_DIR, STATE_FILE);
            if (stateFile.exists() && stateFile.delete()) {
                logger.info("State cleared successfully");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to clear state", e);
        }
    }
}
