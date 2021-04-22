package Dictionary.Update.Insert;

import Dictionary.Program;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class Insertion {
    private String queryText;

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
        var stat = Program.dictionary.getStatement();
        stat.executeUpdate(queryText);
    }

    public final String getQueryText() {
        return queryText;
    }
}
