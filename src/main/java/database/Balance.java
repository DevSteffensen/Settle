package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Balance {
    private Database db;
    // User_ID, balance value
    private HashMap<Integer, Integer> balances;
    // User_ID, (User_ID, value)
    private HashMap<Integer, HashMap<Integer, Integer>> users;

    Balance(Database db) {
        this.db = db;
    }

    public void algorithm(){
        try{
            balances = db.getBalance();

            users = new HashMap<Integer, HashMap<Integer, Integer>>();

            LinkedHashMap<Integer, Integer> debtors = new LinkedHashMap<>();
            LinkedHashMap<Integer, Integer> creditors = new LinkedHashMap<>();


            for (Integer user : balances.keySet()){
                int balance = balances.get(user);

                if (balance < 0){
                    creditors.put(user, balance);

                    // Initiate Table for each user
                    users.put(user, new HashMap<Integer, Integer>());
                }
                else if (balance > 0){
                    // need to make creditors from highest to lowest
                    debtors.putFirst(user, balance);

                    // Initiate Table for each debtor
                    users.put(user, new HashMap<Integer, Integer>());
                }
            }



        } catch (Exception e) {
            System.out.println("Couldn't connect to database, try again later.");
        }

    }
}
