package Dictionary;

import Dictionary.Entities.Translation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class Program {
    public static void main(String[] args) throws Exception{
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Connection conn = null;
        try {
            conn = Common.getConn();
            var stat = conn.createStatement();

            Translation.updScoresToDate(stat);

            //dialog
            System.out.println("Hi there!\nYour last training was on " + ConfigFile.getParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.txt", "last_training"));
            var in = new BufferedReader(new InputStreamReader(System.in));
            do {

                System.out.println("\nSelect what do you want to do:");
                var mainMenuSelections = MainMenuSelections.class.getEnumConstants();
                for (int i = 1; i <= mainMenuSelections.length; i++)
                    System.out.printf("%d. %s\n", i ,mainMenuSelections[i - 1].name);

                int selection = -49 + in.readLine().charAt(0);
                if (selection > -1 && selection < mainMenuSelections.length)
                    mainMenuSelections[selection].action(stat);
            } while (!conn.isClosed());
        }

        catch (Exception e){
            e.printStackTrace();
            if(!conn.isClosed())conn.close();
            System.out.println("Press any key");
            System.in.read();
        }
    }
}
