package Dictionary;

import java.io.BufferedReader;
import java.sql.ResultSet;

public class TableContentPrinter {
    private final String table;
    private final Column[] columns;
    private final String flags;
    private String result;

    public TableContentPrinter(String table, Column[] columns){
        this.table = table;
        this.columns = columns;
        this.flags = "";
    }

    public TableContentPrinter(String table, Column[] columns, String flags){
        this.table = table;
        this.columns = columns;
        this.flags = flags;
    }

    public String getTableContent(){
        if(result == null){
            var sb = new StringBuilder();
            var formatFlags = mergeFormatFlags();
            sb.append(String.format(formatFlags, mergeDisplayingNames()));
            try{
                var query = "SELECT " + getFieldsNames() + " FROM " + table + " " + flags;
                var stat = new Database("dictionary", new ConfigFile(Dictionary.Program.CFG_PATH)).getStatement();
                var queryRes = stat.executeQuery(query);

                int i = 0;
                while (queryRes.next()) {
                    var print = getTupleString(queryRes, ++i);
                    sb.append(print);
                }
                stat.close();
            }catch (Exception e){throw new RuntimeException(e);}
            result = sb.toString();
        }
        return result;
    }

    private String mergeFormatFlags(){
        var sb = new StringBuilder("%-4s ");
        for(var column:columns)
            sb.append(column.getFormatInfo()).append(" ");
        sb.setLength(sb.length() - 1);
        sb.append('\n');
        return sb.toString();
    }
    private String[] mergeDisplayingNames(){
        var res = new String[columns.length + 1];
        res[0] = "num";
        for (int i = 1; i <= columns.length; i++)
            res[i] = columns[i-1].displayingName;
        return res;
    }
    private String getFieldsNames() {
        var sb = new StringBuilder();
        for(var column:columns)
            sb.append(column.dbName).append(',');
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
    private String getTupleString(ResultSet queryRes, int num)throws Exception{
        var args = new String[columns.length + 1];
        args[0] = num + ".";
        for (int i = 1; i < args.length; i++)
            //args[i] = getCell(queryRes, columns[i-1].dbName);
            args[i] = getCell(queryRes, i);
        return String.format(mergeFormatFlags(), args);
    }
    private String getCell(ResultSet queryRes, int columnNumber)throws Exception{
        var res = "";
        var in = new BufferedReader(queryRes.getCharacterStream(columnNumber));
        res = in.readLine();
        return res;
    }

    public static class Column{
        private final String dbName;
        private final String displayingName;
        private final String formatInfo;

        public Column(String dbName, String displayingName, String formatInfo){
            this.dbName = dbName;
            this.displayingName = displayingName;
            this.formatInfo = formatInfo;
        }

        public String getDbName() {
            return dbName;
        }
        public String getDisplayingName() {
            return displayingName;
        }
        public String getFormatInfo() {
            return formatInfo;
        }
    }

    public static void main(String[] args) {
        var test = new TableContentPrinter("dictionary", new Column[]{
                new Column("ukr", "word", "%-25s"),
                new Column("ukr_id", "id", "%-6s"),
                new Column("score", "score", "%-5s"),
                new Column("eng_id", "id", "%-6s"),
                new Column("translate", "word", "%-25s"),
                new Column("last_training", "last trained", "%-10s")}, "ORDER BY score DESC");
        System.out.println(test.getTableContent());

        System.out.println();

        test = new TableContentPrinter("activity_history", new Column[]{
                new Column("score", "score", "%-6s"),
                new Column("day", "date", "%10s")});
        System.out.println(test.getTableContent());
    }
}
