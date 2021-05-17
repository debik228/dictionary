package Dictionary;

import Dictionary.Entities.Translation;
import Dictionary.Menus.MainMenuSelections;
import Dictionary.Menus.MenuHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Program {
    public static ActivityHistory history;
    public static Database dictionary;
    public static final String CFG_PATH = "C:\\user.cfg";

    public static void main(String[] args) throws Exception{
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        try {
            var user = new ConfigFile(CFG_PATH);
            dictionary = new Database(args[0], user);
            var stat = dictionary.getStatement();

            Translation.updScoresToDate(stat);
            history = new ActivityHistory(stat);

            //dialog
            System.out.println("Hi there!\nYour last training was on " + user.params.get("last_upd"));
            MenuHandler.handle(MainMenuSelections.class);
        }

        catch (Exception e){
            e.printStackTrace();
            if(dictionary != null) dictionary.close();
            System.out.println("Press any key");
            System.in.read();
        }
    }
}
