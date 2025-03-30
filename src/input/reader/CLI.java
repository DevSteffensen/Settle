package input.reader;

import java.io.FileReader;
import java.io.IOException;

public class CLI {
    public static void main(String[] args) {
        try{
            Reader reader = new Reader();
            String command;
            while((command = reader.getCommand()) != null) {
                System.out.println(command);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
