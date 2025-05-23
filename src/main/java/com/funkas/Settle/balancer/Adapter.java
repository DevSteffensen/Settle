package com.funkas.Settle.balancer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Adapter {
    private HashMap<Integer, String> translations;
    private Database database;
    private Balance balancer;

    /**
     * Constructs a new Adapter, initializing the database and balance manager,
     * and loads user translations.
     *
     * @throws Exception if initialization or database access fails
     */

    public Adapter() throws Exception {
        this.database = new DatabaseOffline();
        this.balancer = new Balance(database);

        translations = database.getTranslationOfID();
    }


    /**
     * Creates a simple expense transaction where one debtor owes a debtee a specified amount.
     *
     * @param debtor the name of the debtor
     * @param debtee the name of the debtee (creditor)
     * @param value the amount owed
     * @throws Exception if database operations fail
     */

    public void createSimpleExpense(String debtor, String debtee, int value) throws Exception {
        database.addDebt(database.lookupUser(debtor), value);
        database.addDebt(database.lookupUser(debtee), -value);
    }

    /**
     * Creates an expense where multiple debtors owe a single debtee.
     * The total value is split evenly among debtors, with remainder distributed starting from the first.
     *
     * @param debtors array of debtor names
     * @param debtee the debtee (creditor) name
     * @param value the total amount owed
     * @throws Exception if database operations fail
     */

    public void createExpense(String[] debtors, String debtee, int value) throws Exception {
        database.addDebt(database.lookupUser(debtee), -value);

        int numberOfDebtors = debtors.length;

        int baseValue = value / numberOfDebtors;
        int remainder = value % numberOfDebtors;

        for (int i = 0; i < debtors.length; i++) {
            int amountToPay = baseValue;
            if (i < remainder) {
                // distribute the remainder among the first few debtors
                amountToPay += 1;
            }
            database.addDebt(database.lookupUser(debtors[i]), amountToPay);
        }
    }

    /**
     * Creates an expense where multiple debtors owe a debtee with specific values assigned per debtor.
     *
     * @param debtors array of debtor names
     * @param debtee the debtee (creditor) name
     * @param values array of amounts owed by each debtor (must be same length as debtors)
     * @throws Exception if array lengths mismatch or database operations fail
     */

    public void createExpense(String[] debtors, String debtee, int[] values) throws Exception {
        if (debtors.length != values.length) {
            throw new IllegalArgumentException("debtors.length != value.length");
        }
        int totalValue = 0;
        for (int i = 0; i < values.length; i++) {
            totalValue -= values[i];
            database.addDebt(database.lookupUser(debtors[i]), values[i]);
        }
        database.addDebt(database.lookupUser(debtee), totalValue);

    }


    /**
     * Adds a new user to the system and updates the translation map.
     *
     * @param Name the name of the user
     * @return the generated user ID
     * @throws Exception if adding user or lookup fails
     */

    public int addUser(String Name) throws Exception{
        database.addUser(Name);
        int User_ID = database.lookupUser(Name);
        translations.put(User_ID, Name);

        return User_ID;
    }

    /**
     * Returns the total number of users in the system.
     *
     * @return number of users
     */

    public int numberOfUsers(){
        return translations.size();
    }

    /**
     * Clears the database tables and recreates them, resetting the translations map.
     *
     * @throws Exception if database operations fail
     */

    public void clear() throws Exception{
        database.dropTables();
        database.createLedger();
        database.createUsersTable();
        translations = database.getTranslationOfID();
    }

    /**
     * Prints the balance status for a given user, indicating who they owe or who owes them.
     *
     * @param name the username
     * @throws Exception if the user does not exist or database access fails
     */

    public void printBalance(String name) throws Exception {
        try{
            int user_id = database.lookupUser(name);
            HashMap<Integer,  Integer> account = balancer.getBalance(user_id);

            for (Map.Entry<Integer, Integer> entry : account.entrySet()){
                String targetUser = entry.getKey().toString();

                if (translations.containsKey(entry.getKey())){
                    targetUser = translations.get(entry.getKey());
                }

                if (entry.getValue() > 0){
                    System.out.println(name + " owes " + entry.getValue() + "CZK to " + targetUser + ".");
                }
                else{
                    System.out.println(name + " is owed " + -(entry.getValue()) + "CZK from " + targetUser + "." );
                }
            }
        }
        catch(Exception e){
            throw new Exception("name not in database");
        }
    }

    /**
     * Returns a list of all usernames.
     *
     * @return ArrayList of usernames
     */

    public ArrayList<String> getUsers(){
        return new ArrayList<>(translations.values());
    }

    /**
     * Returns the total positive debt summed across all users.
     *
     * @return total debt
     * @throws SQLException if database access fails
     */

    public int getTotalDebt() throws SQLException {
        int totalDebt = 0;
        HashMap<Integer, String> users = database.getTranslationOfID();
        for (int id : users.keySet()) {
            totalDebt += database.getDebt(id);
        }
        return totalDebt;
    }

    /**
     * Returns the total positive debt of a specific user.
     *
     * @param name the username
     * @return the user's total debt
     * @throws SQLException if user lookup or database access fails
     */

    public int getTotalDebt(String name) throws SQLException {
        int user_id = database.lookupUser(name);
        return database.getDebt(user_id);
    }

    /**
     * Returns the average positive debt of all users.
     *
     * @return the average debt
     * @throws SQLException if database access fails
     */
    public float getAverageDebt() throws SQLException {
        return database.getAverageDebt();
    }

    /**
     * Returns the average positive debt for a specific user.
     *
     * @param name the username
     * @return the average debt of the user
     * @throws SQLException if user lookup or database access fails
     */

    public float getAverageDebt(String name) throws SQLException {
        int user_id = database.lookupUser(name);
        return database.getAverageDebt(user_id);
    }
}
