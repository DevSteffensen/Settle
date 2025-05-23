package com.funkas.Settle.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.System.exit;

public class Console {
    public static void main(String[] args) {
        try{
            CommandController commandController = new CommandController();
            console(commandController);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            exit(1);
        }
    }

    public static void console(CommandController commandController){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            commandController.getRequester(br);

            System.out.print("Enter commands (type 'help' for more info): ");
            while (true){
                String input = br.readLine().toLowerCase();
                try{
                    commandController.parseCommand(input);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error with Console, reloading console.");
            console(commandController);
        }
    }
}
