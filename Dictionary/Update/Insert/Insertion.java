package Dictionary.Update.Insert;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class Insertion {
    protected final Statement stat;
    private String queryText;

    public Insertion(Statement stat){
        this.stat = stat;
    }

    protected abstract String formVALUESStatement() throws SQLException;
    protected abstract String formINSERTStatement();

    private void formQueryText() throws SQLException {
        String ValuesStatement = formVALUESStatement();
        String InsertStatement = formINSERTStatement();
        String queryText = "";
        if(!ValuesStatement.isBlank())
            queryText = InsertStatement + ValuesStatement;//.replaceAll("'", "''");
        this.queryText = queryText;
    }
    protected final void executeInsertion() throws SQLException{
        formQueryText();
        stat.executeUpdate(queryText);
    }

    public final String getQueryText() {
        return queryText;
    }
}
