package Dictionary;

import Dictionary.Entities.Word;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class WordContainer {
    public final HashMap<Integer, Word> engWords;
    public final HashMap<Integer, Word> ukrWords;

    public WordContainer(HashMap<Integer, Word> engWords, HashMap<Integer, Word> ukrWords){
        this.engWords = engWords;
        this.ukrWords = ukrWords;
    }

    public static WordContainer loadWords(Statement stmt) throws SQLException {
        var engWords = Common.loadWordTable(stmt, Tables.eng_words);
        var ukrWords = Common.loadWordTable(stmt, Tables.ukr_words);
        return new WordContainer(engWords, ukrWords);
    }
}
