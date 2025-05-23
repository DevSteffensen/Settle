package com.funkas.Settle.balancer;

import java.util.*;

class Balance {
    private Database database;
    // User_ID, balance value
    private HashMap<Integer, Integer> balances;
    // User_ID, (User_ID, value)
    private HashMap<Integer, HashMap<Integer, Integer>> users;

    public Balance(Database db) {
        this.database = db;
    }

    /**
     * Calculates the debts and credits for all users based on their current balances.
     * It separates users into debtors and creditors and populates a map of who owes whom.
     * Uses two-phase settling: exact matches (`settleMatches`) and partial balances (`settle`).
     */

    public void calculateTransactions(){
        try{
            balances = database.getBalance();

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

    /**
     * Resolves debts where a debtor's owed amount exactly matches a creditor's credit.
     * Updates the internal `users` map to reflect these one-to-one matches.
     * Needed, otherwise it won't find the least amount of transactions
     *
     * @param debtors a LinkedHashMap of debtors with their owed values
     * @param creditors a LinkedHashMap of creditors with their credit values (negative balances)
     */

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

    /**
     * Resolves remaining debts where the amounts do not exactly match.
     * Iteratively matches parts of debts and credits until all are settled or iterators are exhausted.
     *
     * @param debtors a LinkedHashMap of remaining debtors and their values
     * @param creditors a LinkedHashMap of remaining creditors and their credit values (negative balances)
     */

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

    /**
     * Retrieves the map of users and the amount they owe or are owed by the given user.
     * Triggers a recalculation if the debts have changed since last fetch.
     *
     * @param User_ID the ID of the user whose balance breakdown is requested
     * @return a HashMap where keys are other user IDs and values are amounts owed (+) or due (-)
     */

    public HashMap<Integer, Integer> getBalance(int User_ID){
        if (database.debtsChanged()){
            calculateTransactions();
        }

        if (!users.containsKey(User_ID)){
            return new HashMap<>();
        }
        else{
            return users.get(User_ID);
        }
    }
}
