package Test;

import Dictionary.ConfigFile;
import Dictionary.Entities.Translation;
import Dictionary.Menus.MainMenuSelections;
import Dictionary.Menus.MenuHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import static Dictionary.Program.CFG_PATH;

public class Program {
    public static void main(String[] args) throws Exception{
        Class.forName("org.postgresql.Driver").getConstructor().newInstance();
        var conn = DriverManager.getConnection("jdbc:postgresql:test", "postgres", "123456789");
        var stat = conn.createStatement();



        Translation.updScoresToDate(stat);            //Don't update score automatically in debug database

        //dialog
        System.out.println("Hi there!\nYour last training was on " + ConfigFile.getParam(CFG_PATH, "last_upd"));
        var in = new BufferedReader(new InputStreamReader(System.in));
        MenuHandler.handle(MainMenuSelections.class, conn);
    }
}
