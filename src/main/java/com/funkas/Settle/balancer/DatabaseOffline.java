package com.funkas.Settle.balancer;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

class DatabaseOffline implements Database{
    private boolean ledgerChanged = true;

    private String url = "jdbc:sqlite:db.sqlite";

    /**
     * Executes a SQL update statement such as CREATE, INSERT, DROP, or UPDATE.
     *
     * @param sql the SQL statement to execute
     * @throws SQLException if a database access error occurs
     */

    private void executeChange(String sql) throws SQLException {
        try(Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    /**
     * Creates the Ledger table if it does not exist.
     *
     * @throws SQLException if a database access error occurs
     */

    public void createLedger() throws SQLException{
        String SQL = "CREATE TABLE IF NOT EXISTS Ledger(User_ID INTEGER, Value INTEGER)";

        executeChange(SQL);
        ledgerChanged = true;
    }

    /**
     * Drops the Ledger and Users tables if they exist.
     *
     * @throws SQLException if a database access error occurs
     */
    public void dropTables() throws SQLException{
        // temporarily hard coded
        String SQL_1 = "DROP TABLE IF EXISTS Ledger";
        String SQL_2 = "DROP TABLE IF EXISTS Users";

        executeChange(SQL_1);
        ledgerChanged = true;
        executeChange(SQL_2);
    }


    /**
     * Adds a debt record for a user with the specified amount.
     *
     * @param ID the user ID
     * @param amount the debt amount (can be positive or negative)
     * @throws SQLException if a database access error occurs
     */

    public void addDebt(int ID, int amount) throws SQLException{
        String SQL = "INSERT INTO Ledger(User_ID, Value) VALUES(" + ID + ", " + amount + ")";

        executeChange(SQL);
        ledgerChanged = true;
    }


    /**
     * Creates the Users table if it does not exist.
     *
     * @throws SQLException if a database access error occurs
     */

    public void createUsersTable() throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS Users( Name TEXT NOT NULL UNIQUE, User_ID INTEGER PRIMARY KEY AUTOINCREMENT)";

        executeChange(sql);
    }

    /**
     * Adds a new user to the Users table.
     *
     * @param name the user's name
     * @throws SQLException if a database access error occurs or the user already exists
     */

    public void addUser(String name) throws SQLException{
        String SQL = "INSERT INTO Users(Name) VALUES (?)";

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    /**
     * Looks up the user ID for a given username.
     *
     * @param name the user's name
     * @return the user ID
     * @throws SQLException if the user is not found or a database access error occurs
     */

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

    /**
     * Returns a map of user IDs to their current balances, ordered from lowest to highest balance.
     *
     * @return a LinkedHashMap with user IDs as keys and their balances as values
     * @throws SQLException if a database access error occurs
     */

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

            // FROM LOWEST TO HIGHEST
            return balances.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e2, LinkedHashMap::new));
        }
    }

    /**
     * Indicates whether the ledger has been updated since the last read.
     *
     * @return true if the ledger has changed, false otherwise
     */

    public boolean debtsChanged(){
        return ledgerChanged;
    }

    /**
     * Retrieves a map translating user IDs to user names.
     *
     * @return a HashMap with user IDs as keys and user names as values
     * @throws SQLException if a database access error occurs
     */

    public HashMap<Integer, String> getTranslationOfID() throws SQLException{
        String SQL = "SELECT User_ID, Name FROM Users";
        HashMap<Integer, String> translation = new HashMap<>();

        try(Connection conn = DriverManager.getConnection(url)){
            PreparedStatement statement = conn.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("User_ID");
                String name = resultSet.getString("Name");
                translation.put(userId, name);
            }

            return translation;
        }
    }

    /**
     * Returns the sum of positive debts for a specific user.
     *
     * @param ID the user ID
     * @return the total positive debt for the user, or 0 if none
     * @throws SQLException if a database access error occurs
     */

    public int getDebt(int ID) throws SQLException{
        String SQL = "SELECT SUM(Value) AS total FROM Ledger WHERE User_ID = " + ID + " AND Value > 0";

        try(Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }

        return 0;
    }

    /**
     * Returns the average positive debt across all users.
     *
     * @return the average positive debt, or 0 if none
     * @throws SQLException if a database access error occurs
     */

    public float getAverageDebt() throws SQLException {
        String SQL = "SELECT AVG(Value) AS averageDebt FROM Ledger WHERE Value > 0";

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // AVG can return null if no rows matched
                float avg = resultSet.getFloat("averageDebt");
                if (resultSet.wasNull()) {
                    // no debt
                    return 0;
                }
                return avg;
            }
        }

        // failed Query
        return 0;
    }

    /**
     * Returns the average positive debt for a specific user.
     *
     * @param ID the user ID
     * @return the average positive debt for the user, or 0 if none
     * @throws SQLException if a database access error occurs
     */

    public float getAverageDebt(int ID) throws SQLException {
        String SQL = "SELECT AVG(Value) AS averageDebt FROM Ledger WHERE Value > 0 AND User_ID = " + ID;

        try (Connection connection = DriverManager.getConnection(url)) {
            PreparedStatement statement = connection.prepareStatement(SQL);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // AVG can return null if no rows matched
                float avg = resultSet.getFloat("averageDebt");
                if (resultSet.wasNull()) {
                    // no debt
                    return 0;
                }
                return avg;
            }
        }

        // failed Query
        return 0;
    }

}
