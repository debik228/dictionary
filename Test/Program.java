package Test;

import Dictionary.Common;
import Dictionary.ConfigFile;
import Dictionary.Entities.Translation;
import Dictionary.MainMenuSelections;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

public class Program {
    public static void main(String[] args) throws Exception{
        Class.forName("org.postgresql.Driver").getConstructor().newInstance();
        var conn = DriverManager.getConnection("jdbc:postgresql:test", "postgres", "123456789");
        var stat = conn.createStatement();

        Translation.updScoresToDate(stat);

        //dialog
        System.out.println("Hi there!\nYour last training was on " + ConfigFile.getParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.txt", "last_training"));
        var in = new BufferedReader(new InputStreamReader(System.in));
        do {

            System.out.println("\nSelect what do you want to do:");
            var mainMenuSelections = MainMenuSelections.class.getEnumConstants();
            for (int i = 1; i <= mainMenuSelections.length; i++)
                System.out.println(i + ". " + mainMenuSelections[i - 1].name());

            int selection = in.readLine().charAt(0);
            selection -= 49;
            if (selection > -1 && selection <= mainMenuSelections.length) {
                mainMenuSelections[selection].action(stat);
            }
        } while (!conn.isClosed());
    }
}
