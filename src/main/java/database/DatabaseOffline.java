package database;

import java.sql.*;

public class DatabaseOffline implements Database{
    private String url = "jdbc:sqlite:db.sqlite";

    private void executeChange(String sql) throws SQLException {
        try(Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public void createTable() throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS Ledger(User_ID INTEGER, value INTEGER)";

        executeChange(sql);
    }

    public void dropTables() throws SQLException{
        String sql = "DROP TABLE IF EXISTS Ledger";

        executeChange(sql);
    }

    public void addDebt(int ID, int amount) throws SQLException{
        String sql = "INSERT INTO Ledger(User_ID, value) VALUES(" + ID + ", " + amount + ")";

        executeChange(sql);
    }

    public int getDebt(int ID) throws SQLException{
        String sql = "SELECT SUM(value) AS total FROM Ledger WHERE User_ID =" + ID;

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }

        return 0;
    }
}
