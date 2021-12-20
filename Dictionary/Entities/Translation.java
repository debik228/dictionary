package Dictionary.Entities;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import Dictionary.ConfigFile;
import Dictionary.Common;
import Dictionary.Program;
import Dictionary.Tables.WordTables;

public class Translation {
    private final int ukr_id;
    private final int eng_id;
    private final UkrWord ukrWord;
    private final EngWord engWord;
    public int score;
    public Calendar last_training;
    private Integer[] successfulTryings;

    private Translation(int ukr_id, int eng_id, UkrWord ukrWord, EngWord engWord, int score, Calendar last_training, Integer[] successfulTryings){
        this.ukr_id = ukr_id;
        this.eng_id = eng_id;
        this.ukrWord = ukrWord;
        this.engWord = engWord;
        this.score = score;
        this.last_training = last_training;
        this.successfulTryings = successfulTryings;
    }

    public static ArrayList<Translation> loadTranslations() throws SQLException{
        return loadTranslations("");
    }

    public static ArrayList<Translation> loadTranslations(String condition) throws SQLException{
        var stat = Program.dictionary.getStatement();
        var res = new ArrayList<Translation>();
        ResultSet queryRes = null;
        String query = "SELECT ukr_id, eng_id, score, last_training, successful_tryings FROM translation" + (condition.length() == 0 ? "" : " WHERE " + condition) + " ORDER BY score ASC;";
        try {
            queryRes = stat.executeQuery(query);
        }catch (SQLException e){
            throw new RuntimeException("SQL exception was occurred. Query: " + query, e);
        }
        HashMap<Integer, Word> ukrWords = Common.loadWordMap(WordTables.ukr_words),
                engWords = Common.loadWordMap(WordTables.eng_words);
        while(queryRes.next()){
            int ukr_id = queryRes.getInt("ukr_id");
            int eng_id = queryRes.getInt("eng_id");
            int score = queryRes.getInt("score");
            Calendar last_training = Calendar.getInstance(); last_training.setTime(queryRes.getDate("last_training"));
            var ukrWord = (UkrWord)ukrWords.get(ukr_id);
            var engWord = (EngWord)engWords.get(eng_id);
            var successfulTryings = (Integer[])queryRes.getArray("successful_tryings").getArray();
            res.add(new Translation(ukr_id, eng_id, ukrWord, engWord, score, last_training, successfulTryings));
        }
        queryRes.close();
        stat.close();
        return res;
    }

    public static void updTranslations(Statement stat, List<Translation> translations) throws  SQLException{
        var query = new StringBuilder("BEGIN;\n");
        for(var trans : translations){
            var str = String.format("UPDATE translation SET score=%d, last_training='%d-%d-%d', successful_tryings = '{%d,%d,%d,%d,%d,%d,%d,%d,%d,%d}' WHERE ukr_id=%d AND eng_id=%d;\n",
                    trans.score, trans.last_training.get(Calendar.YEAR), trans.last_training.get(Calendar.MONTH) + 1, trans.last_training.get(Calendar.DAY_OF_MONTH),
                    trans.successfulTryings[0],trans.successfulTryings[1],trans.successfulTryings[2],trans.successfulTryings[3],trans.successfulTryings[4],trans.successfulTryings[5],trans.successfulTryings[6],trans.successfulTryings[7],trans.successfulTryings[8],trans.successfulTryings[9],
                    trans.ukr_id, trans.eng_id);
            query.append(str);
        }
        query.append("COMMIT;");
        stat.executeUpdate(query.toString());
    }

    public static void updScoresToDate(Statement stmt)throws SQLException, IOException{
        var today = Calendar.getInstance();
        var last_upd = Common.getLastUpd();
        int diff = (365 * (today.get(Calendar.YEAR) - last_upd.get(Calendar.YEAR))) + (today.get(Calendar.DAY_OF_YEAR) - last_upd.get(Calendar.DAY_OF_YEAR)); //на високосні похуй
        var sql = "UPDATE translation SET score = score - " + diff;
        stmt.executeUpdate(sql);
        ConfigFile.setParam(Program.CFG_PATH, "last_upd",
                today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH)+1) + "-" + today.get(Calendar.YEAR));
    }

    public static int getMinScore() throws SQLException{
        return getAggregateVal("SELECT min(score) FROM translation");
    }
    public static int getAvgScore() throws SQLException{
        return getAggregateVal("SELECT avg(score) FROM translation");
    }
    private static int getAggregateVal(String sql)throws SQLException{
        var stat = Program.dictionary.getStatement();
        var qRes = stat.executeQuery(sql);
        qRes.next();
        int res = qRes.getInt(1);
        stat.close();
        return res;
    }

    public Word getWord(WordTables from){
        switch (from){
            case ukr_words:return getUkrWord();
            case eng_words:return getEngWord();
            default: throw new IllegalArgumentException();
        }
    }
    private UkrWord getUkrWord() {
        return ukrWord;
    }
    private EngWord getEngWord() {
        return engWord;
    }

    public int getWordId(WordTables from){
        switch (from){
            case ukr_words:return getUkrId();
            case eng_words:return getEngId();
            default: throw new IllegalArgumentException();
        }
    }
    private int getEngId() {
        return eng_id;
    }
    private int getUkrId() {
        return ukr_id;
    }

    public void addScore(int increase)throws IOException{
        score += increase;
        var today = Calendar.getInstance();
        if(increase <= 0)
            if(!Common.isToday(ConfigFile.getParam(Program.CFG_PATH, "last_upd")))
                ConfigFile.setParam(Program.CFG_PATH, "last_upd",
                        Common.getTodayDate());                                                       //decreasing only in updScoresToDate
            else last_training = today;                                                               //increasing only while training
    }

    public int daysPassedFromAddition(){
        long millis1 = engWord.last_upd.getTimeInMillis();
        long millis2 = ukrWord.last_upd.getTimeInMillis();
        long millisAdd  = Math.max(millis1, millis2);
        long millisCurr = Calendar.getInstance().getTimeInMillis();
        return (int)((millisCurr - millisAdd) / (1000*60*60*24));
    }

    public void insertSuccessfulTrying(int succTry){
        shiftSuccessfulTryingsRight();
        successfulTryings[0] = succTry;
    }
    private void shiftSuccessfulTryingsRight(){
        for(int i = 9; i > 0; i--)
            successfulTryings[i] = successfulTryings[i-1];

    }

    public String toString(){
        return ukr_id + ", " + eng_id + ", " + score + ", " + last_training.get(Calendar.YEAR) + '-' + (last_training.get(Calendar.MONTH)+1) + '-' + last_training.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return ukr_id == that.ukr_id &&
                eng_id == that.eng_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ukr_id, eng_id);
    }
}