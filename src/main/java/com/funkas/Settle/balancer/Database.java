package com.funkas.Settle.balancer;

import java.sql.SQLException;
import java.util.HashMap;

public interface Database {
    void createLedger() throws SQLException;
    void dropTables() throws SQLException;
    void addDebt(int ID, int amount) throws SQLException;
    void createUsersTable() throws SQLException;
    void addUser(String name) throws SQLException;
    int lookupUser(String name) throws SQLException;
    HashMap<Integer, Integer> getBalance() throws SQLException;
    HashMap<Integer, String> getTranslationOfID() throws SQLException;
    boolean debtsChanged();
    int getDebt(int ID) throws SQLException;
    float getAverageDebt() throws SQLException;
    float getAverageDebt(int ID) throws SQLException;
}
