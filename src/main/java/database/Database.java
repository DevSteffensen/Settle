package database;

import java.sql.SQLException;
import java.util.HashMap;

public interface Database {
    void createLedger() throws SQLException;
    void dropTables() throws SQLException;
    void addDebt(int ID, int amount) throws SQLException;
    int getDebt(int ID) throws SQLException;
    void createUsersTable() throws SQLException;
    void addUser(String name) throws SQLException;
    int lookupUser(String name) throws SQLException;
    public HashMap<Integer, Integer> getBalance() throws SQLException;
}
