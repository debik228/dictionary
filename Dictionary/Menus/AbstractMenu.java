package Dictionary.Menus;

import java.sql.Statement;

public interface AbstractMenu {
    public boolean action(Statement stat) throws Exception;
}
