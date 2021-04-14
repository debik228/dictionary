package Dictionary.Menus;

import java.sql.Statement;

public interface AbstractMenu {
    /**
    * @return true if you want to return to previous menu, after execution of code, and false if you want to be return in current menu.
    */
    public boolean action(Statement stat) throws Exception;
}
