package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.Word;
import Dictionary.Tables.RegexTables;
import Dictionary.Tables.Tables;
import Dictionary.Tables.WordTables;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Common {
    public static int getSequanceNextval(Statement statement, String seqName) throws SQLException{
        ResultSet queryRes = statement.executeQuery(String.format("SELECT nextval('%s')", seqName));
        queryRes.next();
        return queryRes.getInt(1);
    }

    public static int getId(WordTables table, String word){
        var stat = Program.dictionary.getStatement();
        int res = -1;
        try{
            var query = String.format("SELECT id FROM %s WHERE word = '%s'", table.toString(), word);
            var queryRes = stat.executeQuery(query);
            queryRes.next();
            res = queryRes.getInt("id");
            stat.close();
            return res;
        }catch (PSQLException e){
            //this means that queryRes is empty (no tuple with such id)
            if(e.getMessage() == "ResultSet not positioned properly, perhaps you need to call next."){}
            else throw new RuntimeException(e);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return res;
    }

    @Deprecated
    public static boolean contains (Tables table, String condition)throws SQLException{
        var stat = Program.dictionary.getStatement();
        var query = String.format("SELECT * FROM %s WHERE %s", table, condition);
        var queryRes = stat.executeQuery(query);
        boolean hasNext = queryRes.next();
        stat.close();
        return hasNext;
    }

    public static int getSuitableWordId(WordTables table, String word)throws SQLException{
        int searchingWordId = -1;
        searchingWordId = seekOutActuallyWord(table, word);
        if(searchingWordId == -1)
            searchingWordId = seekOutRegex(table, word);
        if(searchingWordId == -1)
            searchingWordId = seekOutModifiedRegex(table, word);
        return searchingWordId;
    }
    private static int seekOutActuallyWord(WordTables table, String word)throws SQLException{
        var stat = Program.dictionary.getStatement();
        int searchingWordId = -1;
        var query = String.format("SELECT * FROM %s WHERE word = '%s'", table, word);
        var queryRes = stat.executeQuery(query);
        if(queryRes.next())
            searchingWordId = queryRes.getInt("id");
        stat.close();
        return searchingWordId;
    }
    private static int seekOutRegex(WordTables table, String word)throws SQLException{
        var regexMap = loadRegexTable(table.getAppropriateRegexTable());
        for(var currId : regexMap.keySet())
            if(word.matches(regexMap.get(currId)))
                return currId;
        return -1;
    }
    private static int seekOutModifiedRegex(WordTables table, String word)throws SQLException{
        var wordMap = loadWordMap(table);
        for(var currId : wordMap.keySet()) {
            Word w = wordMap.get(currId);
            if (word.matches(TrainingStatement.modifyString(Difficulty.Hard, w)))
                return currId;
        }
        return -1;
    }

    public static HashMap<Integer, Word> loadWordMap(WordTables table)throws SQLException{
        return loadWordMap(table, null);
    }
    public static HashMap<Integer, Word> loadWordMap(WordTables table, String condition)throws SQLException{
        var stmt = Program.dictionary.getStatement();
        stmt.closeOnCompletion();
        var regexes = loadRegexTable((table == WordTables.ukr_words)?RegexTables.ukr_regex:RegexTables.eng_regex);
        var queryRes = getResultSet(stmt, table, condition);
        var res = new HashMap<Integer, Word>();
        while(queryRes.next()){
            var word = getNextWord(queryRes, table, regexes);
            var id = queryRes.getInt("id");
            res.put(id, word);
        }
        queryRes.close();
        return res;
    }
    private static ResultSet getResultSet(Statement stmt, WordTables table, String Condition) throws SQLException{
        var query = "SELECT id, word, score, pos, last_upd FROM " + table + ( (Condition == null || Condition.length()==0) ? "" : (" WHERE " + Condition) );
        return stmt.executeQuery(query);
    }
    private static Word getNextWord(ResultSet queryRes, WordTables table, HashMap<Integer, String> regexes)throws SQLException{
        var id = queryRes.getInt("id");
        var word = queryRes.getString("word");
        var score = queryRes.getInt("score");
        var pos = Word.PoS.getConstant((String)queryRes.getObject("pos"));
        var regex = regexes.get(id); if(regex == null) regex = word.toLowerCase();
        var last_upd = Calendar.getInstance(); last_upd.setTime(queryRes.getDate("last_upd"));
        try {
            var wordConstructor = table.getAppropriateClass().getConstructor(String.class, int.class, Word.PoS.class, String.class, Calendar.class);//Runtime error after changing word constructor
            return wordConstructor.newInstance(word, score, pos, regex, last_upd);
        }catch (Exception e){throw new RuntimeException(e);}
    }

    public static List<Word> loadWordList(WordTables table)throws SQLException{
        return loadWordList(table, "");
    }
    public static List<Word> loadWordList(WordTables table, String condition)throws SQLException{
        var stmt = Program.dictionary.getStatement();
        stmt.closeOnCompletion();
        var regexes = loadRegexTable((table == WordTables.ukr_words)?RegexTables.ukr_regex:RegexTables.eng_regex);
        var queryRes = getResultSet(stmt, table, condition);
        var res = new LinkedList<Word>();
        while(queryRes.next()){
            var word = getNextWord(queryRes, table, regexes);
            res.add(word);
        }
        queryRes.close();
        return res;
    }

    public static HashMap<Integer, String> loadRegexTable(RegexTables table) throws SQLException{
        var stmt = Program.dictionary.getStatement();
        var res = new HashMap<Integer, String>();
        var sql = "SELECT word_id, regex FROM " + table;
        var queryRes = stmt.executeQuery(sql);
        while (queryRes.next())
            res.put(queryRes.getInt("word_id"), queryRes.getString("regex"));
        stmt.close();
        return res;
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
