package database;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseOffline implements Database{
    private boolean ledgerChanged = true;
    private boolean usersChanged = true;

    private String url = "jdbc:sqlite:db.sqlite";

    private void executeChange(String sql) throws SQLException {
        try(Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public void createLedger() throws SQLException{
        String SQL = "CREATE TABLE IF NOT EXISTS Ledger(User_ID INTEGER, Value INTEGER)";

        executeChange(SQL);
        ledgerChanged = true;
    }

    public void dropTables() throws SQLException{
        // temporarily hard coded
        String SQL_1 = "DROP TABLE IF EXISTS Ledger";
        String SQL_2 = "DROP TABLE IF EXISTS Users";

        executeChange(SQL_1);
        ledgerChanged = true;
        executeChange(SQL_2);
        usersChanged = true;
    }

    public void addDebt(int ID, int amount) throws SQLException{
        String SQL = "INSERT INTO Ledger(User_ID, Value) VALUES(" + ID + ", " + amount + ")";

        executeChange(SQL);
        ledgerChanged = true;
    }

    public int getDebt(int ID) throws SQLException{
        String SQL = "SELECT SUM(Value) AS total FROM Ledger WHERE User_ID =" + ID;

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }

        return 0;
    }

    public void createUsersTable() throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS Users( Name TEXT NOT NULL UNIQUE, User_ID INTEGER PRIMARY KEY AUTOINCREMENT)";

        executeChange(sql);
        usersChanged = true;
    }

    public void addUser(String name) throws SQLException{
        String SQL = "INSERT INTO Users(Name) VALUES (?)";

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);
            statement.setString(1, name);
            statement.executeUpdate();
            usersChanged = true;
        }
    }

    public int lookupUser(String name) throws SQLException{
        String SQL = "SELECT User_ID FROM Users WHERE Name = ?";

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("User_ID");
            }
            else {
                throw new SQLException("User not found");
            }
        }
    }

    public LinkedHashMap<Integer, Integer> getBalance() throws SQLException{
        HashMap<Integer, Integer> balances = new HashMap<>();
        String SQL = "SELECT User_ID , SUM(Value) AS balance FROM Ledger GROUP BY User_ID";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            ledgerChanged = false;

            while (rs.next()) {
                int userId = rs.getInt("User_ID");
                int balance = rs.getInt("balance");
                balances.put(userId, balance);
            }

            return balances.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e2, LinkedHashMap::new));

            // FROM LOWEST TO HIGHEST
        }
    }

    public boolean debtsChanged(){
        return ledgerChanged;
    }

    public boolean usersChanged(){
        return usersChanged;
    }

    public HashMap<Integer, String> getTranslationOfID() throws SQLException{
        String SQL = "SELECT User_ID, Name FROM Users";
        HashMap<Integer, String> translation = new HashMap<>();

        try(Connection conn = DriverManager.getConnection(url)){
            PreparedStatement statement = conn.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();
            usersChanged = false;

            while (resultSet.next()) {
                int userId = resultSet.getInt("User_ID");
                String name = resultSet.getString("Name");
                translation.put(userId, name);
            }

            return translation;
        }
    }
}
