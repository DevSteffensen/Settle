package database;

import java.sql.SQLException;
import java.util.*;

public class Balance {
    private Database db;
    // User_ID, balance value
    private HashMap<Integer, Integer> balances;
    // User_ID, (User_ID, value)
    private HashMap<Integer, HashMap<Integer, Integer>> users;
    private HashMap<Integer, String> translations;

    Balance(Database db) {
        this.db = db;
    }

    public void calculateTransactions(){
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

            settleMatches(debtors, creditors);
            settle(debtors, creditors);

        } catch (Exception e) {
            System.out.println("Couldn't connect to database, try again later.");
        }

    }
    private void settleMatches(LinkedHashMap<Integer, Integer> debtors, LinkedHashMap<Integer, Integer> creditors){
        Map.Entry<Integer, Integer> debtor = null;
        Map.Entry<Integer, Integer> creditor = null;
        int debtorValue = 0;
        int creditorValue = 0;

        Iterator<Map.Entry<Integer, Integer>> debtorIterator = debtors.entrySet().iterator();
        Iterator<Map.Entry<Integer, Integer>> creditorIterator = creditors.entrySet().iterator();

        if (debtorIterator.hasNext()){
            debtor = debtorIterator.next();
        }
        if (creditorIterator.hasNext()){
            creditor = creditorIterator.next();
        }

        while (debtor != null && creditor != null){
            debtorValue = debtor.getValue();
            creditorValue = Math.abs(creditor.getValue());

            if (debtorValue > creditorValue){
                if (debtorIterator.hasNext()){
                    debtor = debtorIterator.next();
                }
                else{
                    break;
                }
            }
            else if (debtorValue == creditorValue){

                users.get(debtor.getKey()).put(creditor.getKey(), debtorValue);
                users.get(creditor.getKey()).put(debtor.getKey(), -debtorValue);

                debtorIterator.remove();
                creditorIterator.remove();

                if (creditorIterator.hasNext() && debtorIterator.hasNext()){
                    creditor = creditorIterator.next();
                    debtor = debtorIterator.next();
                }
                else{
                    break;
                }
            }
            else{
                if (creditorIterator.hasNext()){
                    creditor = creditorIterator.next();
                }
                else{
                    break;
                }
            }
        }
    }

    private void settle(LinkedHashMap<Integer, Integer> debtors, LinkedHashMap<Integer, Integer> creditors){
        Map.Entry<Integer, Integer> debtor = null;
        Map.Entry<Integer, Integer> creditor = null;
        int debtorValue = 0;
        int creditorValue = 0;

        Iterator<Map.Entry<Integer, Integer>> debtorIterator = debtors.entrySet().iterator();
        Iterator<Map.Entry<Integer, Integer>> creditorIterator = creditors.entrySet().iterator();

        if (debtorIterator.hasNext()){
            debtor = debtorIterator.next();
            debtorValue = debtor.getValue();
        }
        if (creditorIterator.hasNext()){
            creditor = creditorIterator.next();
            creditorValue = Math.abs(creditor.getValue());
        }

        while (debtor != null && creditor != null){
            if (debtorValue > creditorValue){

                users.get(debtor.getKey()).put(creditor.getKey(), creditorValue);
                users.get(creditor.getKey()).put(debtor.getKey(), -creditorValue);

                debtorValue -= creditorValue;

                if (creditorIterator.hasNext()){
                    creditor = creditorIterator.next();

                    creditorValue = Math.abs(creditor.getValue());
                }
                else{
                    break;
                }
            }
            else if (debtorValue == creditorValue){
                users.get(debtor.getKey()).put(creditor.getKey(), creditorValue);
                users.get(creditor.getKey()).put(debtor.getKey(), -creditorValue);

                if (creditorIterator.hasNext() && debtorIterator.hasNext()){
                    creditor = creditorIterator.next();
                    debtor = debtorIterator.next();

                    debtorValue = debtor.getValue();
                    creditorValue = Math.abs(creditor.getValue());
                }
                else{
                    break;
                }
            }
            else{
                users.get(debtor.getKey()).put(creditor.getKey(), debtorValue);
                users.get(creditor.getKey()).put(debtor.getKey(), -debtorValue);

                creditorValue -= debtorValue;

                if (debtorIterator.hasNext()){
                    debtor = debtorIterator.next();

                    debtorValue = debtor.getValue();
                }
                else{
                    break;
                }
            }
        }
    }

    public void printBalance(int User_ID){

        if (db.usersChanged()){
            try{
                translations = db.getTranslationOfID();
            }
            catch (SQLException e){
                System.out.println("Couldn't connect to database, try again later.");
            }
        }

        if (db.debtsChanged()){
            calculateTransactions();
        }

        if (!users.containsKey(User_ID)){
            System.out.println("User has 0 debt.");
            System.out.println("User is owed 0 debt");
        }

        else{
            for (Map.Entry<Integer, Integer> entry : users.get(User_ID).entrySet()){
                String name = entry.getKey().toString();

                if (translations.containsKey(entry.getKey())){
                    name = translations.get(entry.getKey());
                }

                if (entry.getValue() > 0){
                    System.out.println("User owes " + entry.getValue() + "$ to " + name + ".");
                }
                else{
                    System.out.println("User is owed " + -(entry.getValue()) + "$ from " + name + "." );
                }
            }
        }
    }
}
