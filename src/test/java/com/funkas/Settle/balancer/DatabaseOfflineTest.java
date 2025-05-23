package com.funkas.Settle.balancer;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseOfflineTest {

    private DatabaseOffline db;

    @BeforeEach
    void setup() throws SQLException {
        db = new DatabaseOffline();
        cleanup();
    }

    @Test
    @Order(1)
    void testAddAndLookupUser() throws SQLException {
        db.addUser("Alice");
        int id = db.lookupUser("Alice");
        assertEquals(1, id);
    }

    @Test
    @Order(2)
    void testAddDebtAndGetDebt() throws SQLException {
        db.addUser("Bob");
        int id = db.lookupUser("Bob");
        db.addDebt(id, 50);
        db.addDebt(id, 30);
        assertEquals(80, db.getDebt(id));
    }

    @Test
    @Order(3)
    void testGetBalanceSorted() throws SQLException {
        db.addUser("Carl");
        db.addUser("Dana");

        int idCarl = db.lookupUser("Carl");
        int idDana = db.lookupUser("Dana");

        db.addDebt(idCarl, 100);
        db.addDebt(idDana, 50);

        LinkedHashMap<Integer, Integer> balances = db.getBalance();
        Integer[] ids = balances.keySet().toArray(new Integer[0]);

        assertEquals(idDana, ids[0]); // Dana should come first due to lower balance
        assertEquals(idCarl, ids[1]); // Carl should come second
    }

    @Test
    @Order(5)
    void testDebtsChangedFlag() throws SQLException {
        assertTrue(db.debtsChanged());
        db.getBalance(); // This resets the flag
        assertFalse(db.debtsChanged());

        db.addUser("Frank");
        int id = db.lookupUser("Frank");
        db.addDebt(id, 10);

        assertTrue(db.debtsChanged());
    }

    @AfterEach
    void cleanup() throws SQLException {
        db.dropTables(); // Ensure clean state
        db.createUsersTable();
        db.createLedger();
    }
}