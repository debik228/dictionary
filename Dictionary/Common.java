package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Common {
    public static Connection getConn(){
        Connection conn = null;
        try{
            Class.forName("org.postgresql.Driver").getConstructor().newInstance();
            conn = DriverManager.getConnection("jdbc:postgresql:dictionary", "postgres", "123456789");
        }catch (Exception e){System.err.println(e);}
        return conn;
    }

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

    public static HashMap<Integer, Word> loadWordTable(Statement stmt, Tables table)throws SQLException{
        if(table != Tables.eng_words && table != Tables.ukr_words) throw new IllegalArgumentException(table.toString() + " isn't a word table");
        var res = new HashMap<Integer, Word>();
        var wordClass = table == Tables.ukr_words? UkrWord.class : EngWord.class;
        var queryRes = stmt.executeQuery("SELECT id, word, score, last_upd FROM " + table);
        while(queryRes.next()){
            var id = queryRes.getInt("id");
            var word = queryRes.getString("word");
            var score = queryRes.getInt("score");
            try {
                res.put(id, wordClass.getConstructor(String.class, int.class).newInstance(word, score));
            }catch (Exception e){throw new RuntimeException(e);}
        }
        queryRes.close();
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
}
