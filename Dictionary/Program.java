package Dictionary;

import Dictionary.Entities.Translation;
import Dictionary.Menus.MainMenuSelections;
import Dictionary.Menus.MenuHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class Program {
    public static ActivityHistory history;
    public static Database dictionary;
    public static final String CFG_PATH = "C:\\user.cfg";

    public static void main(String[] args) throws Exception{
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Connection conn = null;
        try {
            var user = new ConfigFile(CFG_PATH);
            dictionary = new Database("dictionary", user);
            conn = dictionary.getConn();
            var stat = conn.createStatement();//TODO завжди передавати connection замість statement як параметр

            Translation.updScoresToDate(stat);
            history = new ActivityHistory(stat);

            //dialog
            System.out.println("Hi there!\nYour last training was on " + user.params.get("last_upd"));
            MenuHandler.handle(MainMenuSelections.class, conn);
        }

        catch (Exception e){
            e.printStackTrace();
            if(conn != null && !conn.isClosed()) conn.close();
            System.out.println("Press any key");
            System.in.read();
        }
    }
}
