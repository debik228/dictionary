package Dictionary.Table;

import java.sql.Statement;
import java.util.HashMap;

public abstract class Table {
    public HashMap<Object, Object> tableData;
    public Table table;

    public abstract void loadTable(Statement stat);
    public abstract void saveTable(Statement stat);
}
