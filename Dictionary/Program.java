package Dictionary;

import Dictionary.Entities.Translation;
import Dictionary.Menus.MainMenuSelections;
import Dictionary.Menus.MenuHandler;

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
            System.out.println("Hi there!\nYour last training was on " + ConfigFile.getParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.cfg", "last_upd"));
            MenuHandler.handle(MainMenuSelections.class, conn);
        }

        catch (Exception e){
            e.printStackTrace();
            if(!conn.isClosed())conn.close();
            System.out.println("Press any key");
            System.in.read();
        }
    }
}
