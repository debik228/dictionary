package Dictionary.Update.Insert;

import Dictionary.Common;
import Dictionary.Tables.WordTables;

import java.sql.SQLException;
import java.sql.Statement;

public class WordInsertion extends Insertion{
    public final String[] words;
    public final int[] correspondingIds;
    public final boolean[] hasJustInserted;
    private final WordTables table;

    public WordInsertion(Statement stat, String[] words, WordTables table)throws SQLException {
        super(stat);
        this.words = words;
        this.correspondingIds = new int[words.length];
        this.hasJustInserted = new boolean[words.length];
        this.table = table;
        fillInIdArray();
        executeInsertion();
        fillInStubs();
    }

    private void fillInIdArray()throws SQLException{
        for(int i = 0; i < words.length; i++)
            correspondingIds[i] = Common.getSuitableWordId(stat, table, words[i]);
    }

    protected String formVALUESStatement() {
        var VALUES = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (hasCorrespondingId(i)) continue;
            VALUES.append(
                    String.format("('%s'),", words[i]));
        }
        if (VALUES.length() > 0) {
            VALUES.setCharAt(VALUES.length() - 1, '\n');
            return VALUES.toString();
        }
        else return "";
    }
    private boolean hasCorrespondingId(int index){
        return correspondingIds[index] > -1;
    }

    protected String formINSERTStatement(){
        return "INSERT INTO " + table + " (word) VALUES";
    }

    private void fillInStubs(){
        for (int i = 0; i < correspondingIds.length; i++) {
            if(hasCorrespondingId(i)) continue;
            correspondingIds[i] = Common.getId(stat, table, words[i]);
            hasJustInserted[i] = true;
        }
    }
}
