package Dictionary.Menus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;

public class MenuHandler {
    public static void handle(Class<? extends AbstractMenu> menu, Connection conn)throws Exception{
        var stat = conn.createStatement();
        var in = new BufferedReader(new InputStreamReader(System.in));
        boolean moveBack = false;
        do {

            System.out.println("\nSelect what do you want to do:");
            var menuSelections = menu.getEnumConstants();
            for (int i = 1; i <= menuSelections.length; i++)
                System.out.printf("%d. %s\n", i , menuSelections[i - 1]);

            int selection = -49 + in.readLine().charAt(0);
            if (selection > -1 && selection < menuSelections.length)
                moveBack = menuSelections[selection].action(stat);
        } while (!moveBack);
    }
}
