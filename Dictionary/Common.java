package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;

public class Common {
    public static int getSequanceCurrval(Statement statement, String seqName) throws SQLException{
        ResultSet queryRes = null;
        try {
            queryRes = statement.executeQuery(String.format("SELECT currval('%s')", seqName));
        } catch (SQLException e){
            System.err.println(e);
            queryRes = statement.executeQuery(String.format("SELECT nextval('%s')", seqName));
        }
        queryRes.next();
        return queryRes.getInt(1);
    }

    public static int getId(Statement stat, Tables table, String word)throws SQLException{
        var query = String.format("SELECT id FROM %s WHERE word = '%s'", table.toString(), word);
        var queryRes = stat.executeQuery(query);
        queryRes.next();
        int res = queryRes.getInt("id");
        queryRes.close();
        return res;
    }

    public static boolean contains (Statement stat, Tables table, String condition)throws SQLException{
        var query = String.format("SELECT * FROM %s WHERE %s", table, condition);
        var queryRes = stat.executeQuery(query);
        return queryRes.next();
    }

    public static HashMap<Integer, String> loadRegexTable(Statement stmt, Tables table) throws SQLException{
        if(table != Tables.ukr_regex && table != Tables.eng_regex) throw new IllegalArgumentException();
        var res = new HashMap<Integer, String>();
        var sql = "SELECT word_id, regex FROM " + table;
        var queryRes = stmt.executeQuery(sql);
        while (queryRes.next())
            res.put(queryRes.getInt("word_id"), queryRes.getString("regex"));
        return res;
    }

    public static HashMap<Integer, Word> loadWordTable(Statement stmt, Tables table, String Condition)throws SQLException{
        if(table != Tables.eng_words && table != Tables.ukr_words) throw new IllegalArgumentException(table.toString() + " isn't a word table");
        var regexes = loadRegexTable(stmt, (table == Tables.ukr_words)?Tables.ukr_regex:Tables.eng_regex);
        var res = new HashMap<Integer, Word>();
        var wordClass = table == Tables.ukr_words? UkrWord.class : EngWord.class;
        var query = "SELECT id, word, score, pos FROM " + table + ( (Condition == null || Condition.length()==0) ? "" : (" WHERE " + Condition) );
        var queryRes = stmt.executeQuery(query);
        while(queryRes.next()){
            var id = queryRes.getInt("id");
            var word = queryRes.getString("word");
            var score = queryRes.getInt("score");
            var pos = Word.PoS.getConstant((String)queryRes.getObject("pos"));
            var regex = regexes.get(id); if(regex == null) regex = word;
            try {
                res.put(id, wordClass.getConstructor(String.class, int.class, Word.PoS.class, String.class).newInstance(word, score, pos, regex));
            }catch (Exception e){throw new RuntimeException(e);}
        }
        queryRes.close();
        return res;
    }

    public static HashMap<Integer, Word> loadWordTable(Statement stmt, Tables table)throws SQLException{
        return loadWordTable(stmt, table, null);
    }

    public static boolean isToday(String dateStr){
        var lastTrainingDate = dateStr.split("-");
        var today = Calendar.getInstance();
        if(Integer.toString(today.get(Calendar.DAY_OF_MONTH)).equals(lastTrainingDate[0])
                && Integer.toString(today.get(Calendar.MONTH)).equals(lastTrainingDate[1])
                && Integer.toString(today.get(Calendar.YEAR)).equals(lastTrainingDate[2]))
            return true;
        return false;
    }

    //TODO replace this method with ConfigFile.getParamDate(String paramName) and static ConfigFile.getParamDate(String path, String paramName)
    public static Calendar getLastUpd() throws IOException {
        var res = Calendar.getInstance();
        var last_updParam = ConfigFile.getParam(Program.CFG_PATH, "last_upd").split("-");
        res.set(Integer.parseInt(last_updParam[2]), Integer.parseInt(last_updParam[1]) - 1, Integer.parseInt(last_updParam[0]));
        return res;
    }

    public static boolean sameDate(Calendar date1, Calendar date2){
        return date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR)
                && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);
    }

    public static String getTodayDate(){
        var today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH)+1) + "-" + today.get(Calendar.YEAR);
    }
}
