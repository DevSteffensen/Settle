package input.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;

public class Reader{
    BufferedReader consoleReader;

    public Reader() throws Exception {
        try{
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception e) {
            throw new Exception("Error initializing console");
        }
    }

    public String getCommand(){
        try{
            return consoleReader.readLine();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public LinkedList<String> getCommandsFromFile(String fileName){
        LinkedList<String> commands = new LinkedList<>();
        Path filePath = Path.of(fileName);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null){
                commands.add(line);
            }
        }
        catch(Exception e) {
            System.out.println("Error: Couldn't read file");
            return null;
        }

        return commands;
    }
}
