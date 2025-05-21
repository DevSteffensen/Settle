package database;

import java.sql.SQLException;

public interface Database {
    void createTable() throws SQLException;
    void dropTables() throws SQLException;
    void addDebt(int ID, int amount) throws SQLException;
    int getDebt(int ID) throws SQLException;
}
