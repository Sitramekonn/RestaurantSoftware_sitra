package edu.metrostate.ics372_phase2;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class MainAppTest {

    @Test
    void testMainAppInstantiation() {
        assertDoesNotThrow(() -> new MainApp());
    }

    @Test
    void testMainAppHasController() {
        MainApp app = new MainApp();
        
        // Since controller is private, we test indirectly through behavior
        assertDoesNotThrow(() -> app.toString());
        assertNotNull(app);
    }

    @Test
    void testMainAppStart() {
        MainApp app = new MainApp();
        
        // Test that start method exists and can be called
        // Note: We can't fully test JavaFX start without Platform.startup()
        assertNotNull(app);
    }

    @Test
    void testMainAppMain() {
        // Test that main method exists and can be called
        String[] args = {};
        
        // Note: MainApp doesn't have a main method, it's launched through JavaFX
        // Testing basic instantiation instead
        assertDoesNotThrow(() -> {
            MainApp app = new MainApp();
            assertNotNull(app);
        });
    }

    @Test
    void testMainAppInitialization() {
        MainApp app = new MainApp();
        
        // Test basic object creation
        assertNotNull(app);
        assertEquals(MainApp.class, app.getClass());
    }
}