package com.funkas.Settle.console;


import com.funkas.Settle.balancer.Adapter;

import java.io.BufferedReader;
import java.util.ArrayList;
import static java.lang.System.exit;


/**
 * CommandController handles command parsing and user interaction for the console-based
 * expense balancing system. It uses an Adapter to manage data operations.
 */

public class CommandController {
    BufferedReader requester;
    Adapter adapter;


    /**
     * Constructs a CommandController and initializes the Adapter.
     * If Adapter fails to initialize, the program exits.
     */
    public CommandController() {
        try{
            adapter = new Adapter();
        }
        catch(Exception e){
            System.out.println("Couldn't load adapter: " + e.getMessage());
            exit(1);
        }
    }

    /**
     * Sets the BufferedReader used to receive user input.
     *
     * @param requester BufferedReader for console input
     */

    public void getRequester(BufferedReader requester){
        this.requester = requester;
    }

    /**
     * Parses and executes commands input by the user.
     *
     * @param command the command string to parse
     */
    public void parseCommand(String command){
        switch (command) {
            case "help":
                System.out.println("'add user' : command for adding a user to database");
                System.out.println("(only the users known to the system can be owed a debt and owe a debt)\n");
                System.out.println("'get users' : command for listing all users.\n");
                System.out.println("'add simple expense' : command for adding an expense to the system).");
                System.out.println("(it asks you a question to select a payer, then value and lastly payee)\n");
                System.out.println("'add expense' : command for adding a complex expense (split between multiple debtors or with different values owed).\n");
                System.out.println("'get balance' : command for getting info about who owes what debt.\n");
                System.out.println("'total traffic' : Number of CZK tracked in the system.\n");
                System.out.println("'total debt' : How much user spent through this system.\n");
                System.out.println("'average transaction' : average debt of single transaction.\n");
                System.out.println("'average debt' : average debt of a single user.\n");
                System.out.println("'drop tables' : delete all information from databases.\n");
                System.out.println("'drop tables' : delete all information from databases.\n");
                System.out.println("'exit' : command to exit the program.");
                System.out.println("-------------------------------------");

                break;

            case "add user":
                while (true){
                    try{
                        System.out.println("type the name of the user you wish to add. ('end' to exit the operation)");
                        String name = requester.readLine().trim();

                        if (name.equals("end")){
                            System.out.println("Operation aborted.");
                            return;
                        }

                        System.out.println("Is this the user you wish to add? {" + name + "} (Y/n)");

                        String response = requester.readLine().trim();

                        if (response.equals("y") || response.equals("yes") || response.isEmpty()){
                            try{
                                adapter.addUser(name);
                                System.out.println("User added - " + name);
                            }
                            catch (Exception e){
                                System.out.println("User already exists.");
                            }
                            return;
                        }
                    }
                    catch(Exception e){
                        System.out.println("Console failure, try again.");
                    }

                }

            case "add simple expense":
                if (adapter.numberOfUsers() < 2){
                    System.out.println("Not enough people in this group");
                    break;
                }

                System.out.println();
                System.out.println("Which user payed the expense?");

                String debtee;
                int amount;
                String debtor;

                try{
                    debtee = requestName();
                    System.out.println("How much was paid? (only numbers, 'end' to stop adding the expense)");
                    amount = requestValue();
                    debtor = requestName(debtee);

                }
                catch (Exception e){
                    System.out.println("Operation aborted.");
                    break;
                }

                try{
                    adapter.createSimpleExpense(debtor, debtee, amount);
                    System.out.println("Expense Added");
                }
                catch (Exception e){
                    System.out.println("Operation failed.");
                }
                break;

            case "get balance":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("Not enough people in this group");
                    break;
                }

                try{
                    System.out.println("Do you wish to see global balance? (Y/n) {'end' to stop looking at balances}");

                    String response = requester.readLine().trim();
                    if (response.equals("end")){
                        System.out.println("Operation aborted.");
                        break;
                    }

                    if (response.equals("y") || response.equals("yes") || response.isEmpty()){
                        try{
                            for (String user : adapter.getUsers()){
                                adapter.printBalance(user);
                                System.out.println("------------------------------");
                            }
                        }catch (Exception e){
                            System.out.println("Operation failed.");
                            break;
                        }
                    }
                    else{
                        try{
                            System.out.println("Type 1 username from the list to get a balance of that account:");
                            String name = requestName();
                            adapter.printBalance(name);
                        }
                        catch (Exception e){
                            System.out.println("Operation failed.");
                        }
                    }

                }catch (Exception e){
                    System.out.println("Console failure, try again.");

                }
                break;

            case "add expense":
                if (adapter.numberOfUsers() < 2){
                    System.out.println("Not enough people in this group");
                    break;
                }

                try{
                    System.out.println("Split one value across all debtors? (Y/n)");

                    String response = requester.readLine().trim();

                    if (response.equals("Y") || response.equals("y") || response.equals("yes") || response.isEmpty()){
                        try{
                            System.out.println("Which user payed the expense?");
                            debtee = requestName();
                            System.out.println("How much was paid? (only numbers, 'end' to stop adding the expense)");
                            amount = requestValue();
                            System.out.print("Who owes debt?");
                            String[] debtors = requestNames(debtee);
                            adapter.createExpense(debtors, debtee, amount);
                            System.out.println("Expense Added");
                        }
                        catch (Exception e){
                            System.out.println("Operation aborted.");
                            break;
                        }
                    }
                    else{
                        try{
                            System.out.println("Which user payed the expense?");
                            debtee = requestName();
                            System.out.print("Who owes debt?");
                            String[] debtors = requestNames(debtee);
                            System.out.println("How much was paid? (only numbers, 'end' to stop adding the expense)");
                            int[] amounts = requestValues(debtors);
                            adapter.createExpense(debtors, debtee, amounts);
                            System.out.println("Expense Added");
                        }
                        catch (Exception e){
                            System.out.println("Operation aborted.");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Console failure, try again.");
                }
                break;
            case "get users":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("No users registered in system.");
                    return;
                }

                for(String name : adapter.getUsers()){
                    System.out.print("'" + name + "' ");
                }
                System.out.println();
                break;

            case "drop tables":
                try{
                    System.out.println("All tables dropped.");
                    adapter.clear();
                }
                catch (Exception e){
                    System.out.println("Operation failed.");
                }
                break;

            case "total traffic":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("Not enough people in this group");
                    break;
                }
                try {
                    System.out.println("Total traffic is: " + adapter.getTotalDebt());
                } catch (Exception e){
                    System.out.println("Operation failed.");
                }
                break;

            case "total debt":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("Not enough people in this group");
                    break;
                }
                try{
                    String name = requestName();
                    System.out.println("Total debts of " + name + ": " + adapter.getTotalDebt(name));
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }

                break;

            case "average transaction":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("Not enough people in this group");
                    break;
                }
                try {
                    System.out.println("Average debts: " + adapter.getAverageDebt() + "CZK.");
                }
                catch(Exception e){
                    System.out.println("Operation failed.");
                }
                break;

            case "average debt":
                if (adapter.numberOfUsers() == 0){
                    System.out.println("Not enough people in this group");
                    break;
                }
                try{
                String name = requestName();
                System.out.println("Average debt of " + name + ": " + adapter.getAverageDebt(name) + "CZK.");
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }

                break;

            case "exit":
                exit(0);
            default:
                System.out.println("Unknown command. Type 'help' to see available commands.");
        }
    }

    /**
     * Requests a username from the console input.
     * Repeats until a valid username from the system is entered or 'end' to abort.
     * Only a helper function
     *
     * @return the valid username
     * @throws Exception if 'end' is entered to abort the command
     */
    private String requestName() throws Exception{
        ArrayList<String> users = adapter.getUsers();

        for (String user : users){
            System.out.print("'" + user + "' ");
        }
        System.out.println();
        System.out.println("(type 'end' to stop the operation)");

        while (true){
            String name = requester.readLine().trim();
            if (users.contains(name)){
                return name;
            }
            else if (name.equals("end")){
                throw new Exception("command aborted.");
            }
            System.out.println("Name not recognised, try again.");
        }
    }

    /**
     * Only a reskinned function from above.
     * This function doesn't accept a name in parameter
     *
     * @param name username to exclude from selection
     * @return a valid username different from 'name'
     * @throws Exception if 'end' is entered to abort or if name not recognized
     */

    private String requestName(String name) throws Exception{
        ArrayList<String> users = adapter.getUsers();

        for (String user : users){
            if (!name.equals(user)){
                System.out.print("'" + user + "' ");
            }
        }
        System.out.println();
        System.out.println("(type 'end' to stop the operation)");


        while (true){
            String user = requester.readLine().trim();
            if (users.contains(name) && !user.equals(name)){
                return user;
            }
            else if (name.equals("end")){
                throw new Exception("command aborted.");
            }
            System.out.println("Name not recognised, try again.");
        }
    }

    /**
     * Requests a positive integer value from the console input.
     * Repeats until a valid positive integer is entered or 'end' to abort.
     *
     * @return the requested integer value
     * @throws Exception if 'end' is entered to abort
     */

    private int requestValue() throws Exception{
        int amount;

        while (true){
            String response = requester.readLine().trim();

            if (response.equals("end")){
                throw new Exception("command aborted.");
            }

            try{
                amount = Integer.parseInt(response);
                if (amount < 0){
                    throw new Exception("amount must be a positive integer.");
                }
                break;
            }
            catch (NumberFormatException e){
                System.out.println("Number not recognised, use numerical (example: '100')");
            }
        }
        return amount;
    }

    /**
     * Prompts the user to enter names of debtors, one by one, excluding the creditor.
     * Continues until the user presses enter with no input (finalizing selection),
     * or types 'end' to abort the operation.
     *
     * @param creditor the name of the user who paid and should be excluded from the debtor list
     * @return an array of debtor names selected by the user
     * @throws Exception if the user types 'end' or no valid names are selected
     */

    private String[] requestNames(String creditor) throws Exception{
        ArrayList<String> users = adapter.getUsers();
        ArrayList<String> debtors = new ArrayList<>();

        System.out.println();
        System.out.println("(type 'end' to stop the operation AND enter to stop selection)");

        while (true){
            boolean canChoose = false;
            for (String user : users){
                if (!creditor.equals(user) && !debtors.contains(user)){
                    canChoose = true;
                    System.out.print("'" + user + "' ");
                }
            }
            if (canChoose){
                System.out.println();
            }
            else{
                return debtors.toArray(new String[0]);
            }

            String user = requester.readLine().trim();
            if (user.isEmpty()){
                if (debtors.isEmpty()){
                    throw new Exception("command aborted.");
                }
                return debtors.toArray(new String[0]);
            }
            else if (user.equals("end")){
                throw new Exception("command aborted.");
            }
            else if (users.contains(user) && !creditor.equals(user) && !debtors.contains(user)){
                debtors.add(user);
            }
        }
    }

    /**
     * Prompts the user to enter a series of integer values corresponding to individual debts.
     * The number of values required is defined by the input parameter `count`.
     *
     * @param count the number of values to request from the user
     * @return an array of integers entered by the user
     * @throws Exception if the user types 'end' during the input sequence
     */

    private int[] requestValues(String[] debtors) throws Exception{
        int[] values = new int[debtors.length];
        for (int i = 0; i < debtors.length; i++){
            System.out.println("How much does '" + debtors[i] + "' owe?");
            values[i] = requestValue();
        }
        return values;
    }
}
