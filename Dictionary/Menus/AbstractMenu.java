package Dictionary.Menus;

import java.sql.Statement;

public interface AbstractMenu {
    public void action(Statement stat) throws Exception;
}
