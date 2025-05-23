package com.funkas.Settle.balancer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class BalanceTest {

    private DatabaseOffline db;
    private Balance balance;
    ArrayList<String> names;
    ArrayList<Integer> ids;
    ArrayList<Integer> debts;


    @BeforeEach
    void setup() throws SQLException {
        this.db = new DatabaseOffline();
        cleanup();

        String[] names = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        this.names = new ArrayList<String>(List.of(names));
        int[] ids = new int[names.length];
        int[] debts = new int[]{100, 20, 30, 90, 80, -160, -20, -50, -30, -60};
        this.debts = Arrays.stream(debts).boxed().collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < names.length; i++){
            db.addUser(names[i]);
            ids[i] = db.lookupUser(names[i]);
            db.addDebt(ids[i], debts[i]);
        }

        this.ids = Arrays.stream(ids).boxed().collect(Collectors.toCollection(ArrayList::new));

        /*
          Scenario:
          - A owes 100
          - B owes 20
          - C owes 30
          - D owes 90
          - E owes 80
          - F is owed 160
          - G is owed 20
          - H is owed 50
          - I is owed 30
          - J is owed 60
         */

        balance = new Balance(db);
    }

    @Test
    @Order(1)
    public void testTransactionCalculation() {

        /*
         B -> G 20  {1 -> 6}
         C -> I 30 {2 -> 8}

         A100, D90, E80
         F160, J60, H50

         A -> F 100 {0 -> 5}
         D -> F 60 {3 -> 5}
         D -> J 30 {3 -> 9}
         E -> J 30 {4 -> 9}
         E -> H 50 {4 -> 7}
         */

        ArrayList<HashMap<Integer, Integer>> transactions = new ArrayList<>();
        for (int id : ids){
            transactions.add(balance.getBalance(id));
        }

        assertEquals(1, transactions.get(0).size());
        assertEquals(1, transactions.get(1).size());
        assertEquals(1, transactions.get(2).size());
        assertEquals(2, transactions.get(3).size());
        assertEquals(2, transactions.get(4).size());
        assertEquals(2, transactions.get(5).size());
        assertEquals(1, transactions.get(6).size());
        assertEquals(1, transactions.get(7).size());
        assertEquals(1, transactions.get(8).size());
        assertEquals(2, transactions.get(9).size());

        assertEquals(20, transactions.get(1).get(ids.get(6)));
        assertEquals(-20, transactions.get(6).get(ids.get(1)));

        assertEquals(30, transactions.get(2).get(ids.get(8)));
        assertEquals(-30, transactions.get(8).get(ids.get(2)));

        assertEquals(100, transactions.get(0).get(ids.get(5)));
        assertEquals(-100, transactions.get(5).get(ids.get(0)));

        assertEquals(60, transactions.get(3).get(ids.get(5)));
        assertEquals(-60, transactions.get(5).get(ids.get(3)));

        assertEquals(30, transactions.get(3).get(ids.get(9)));
        assertEquals(-30, transactions.get(9).get(ids.get(3)));

        assertEquals(30, transactions.get(4).get(ids.get(9)));
        assertEquals(-30, transactions.get(9).get(ids.get(4)));

        assertEquals(50, transactions.get(4).get(ids.get(7)));
        assertEquals(-50, transactions.get(7).get(ids.get(4)));
    }


    @Test
    @Order(2)
    public void testNoTransactionsForNeutralUser() throws SQLException {
        db.addUser("Dave");
        int daveId = db.lookupUser("Dave");

        HashMap<Integer, Integer> daveTransactions = balance.getBalance(daveId);
        assertTrue(daveTransactions.isEmpty(), "Neutral user should have no transactions.");
    }

    @Test
    @Order(3)
    public void testBiggestSpender() throws SQLException {
        // test that "settling" a debt doesn't do anything
        db.addDebt(ids.get(0), -100);

        db.addDebt(ids.get(0), -20);
        db.addDebt(ids.get(0), 50);
        db.addDebt(ids.get(0), 20);
        db.addDebt(ids.get(0), 30);

        // Total 200 (100 + 50 +20 + 30)
        assertEquals(200, db.getDebt(ids.get(0)));
    }

    @AfterEach
    void cleanup() throws SQLException {
        db.dropTables();
        db.createUsersTable();
        db.createLedger();
    }
}