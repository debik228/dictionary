package Dictionary.Menus;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MenuHandler {
    public static void handle(Class<? extends AbstractMenu> menu)throws Exception{
        var in = new BufferedReader(new InputStreamReader(System.in));
        boolean moveBack = false;
        do {

            System.out.println("\nSelect what do you want to do:");
            var menuSelections = menu.getEnumConstants();
            for (int i = 1; i <= menuSelections.length; i++)
                System.out.printf("%d. %s\n", i , menuSelections[i - 1]);

            var response = in.readLine();
            if(response.length() > 0){
                int selection = -49 + response.charAt(0);
                if (selection > -1 && selection < menuSelections.length)
                    moveBack = menuSelections[selection].action();
            }
        } while (!moveBack);
    }
}
