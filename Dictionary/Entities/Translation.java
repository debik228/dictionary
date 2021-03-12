package Dictionary.Entities;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import Dictionary.ConfigFile;
import Dictionary.Common;

public class Translation {
    public final int ukr_id;
    public final int eng_id;
    public int score;
    public Calendar last_training;

    public Translation(int ukr_id, int eng_id, int score, Calendar last_training){
        this.ukr_id = ukr_id;
        this.eng_id = eng_id;
        this.score = score;
        this.last_training = last_training;
    }

    public static ArrayList<Translation> loadTranslations(Statement stat) throws SQLException{
        return loadTranslations(stat, "");
    }

    public static ArrayList<Translation> loadTranslations(Statement stat, String condition) throws SQLException{
        var res = new ArrayList<Translation>();
        ResultSet queryRes = null;
        String query = "SELECT ukr_id, eng_id, score, last_training FROM translation" + (condition.length() == 0 ? "" : " WHERE " + condition) + " ORDER BY score ASC;";
        try {
            queryRes = stat.executeQuery(query);
        }catch (SQLException e){
            throw new RuntimeException("SQL exception was occurred. Query: " + query, e);
        }
        while(queryRes.next()){
            int ukr_id = queryRes.getInt("ukr_id");
            int eng_id = queryRes.getInt("eng_id");
            int score = queryRes.getInt("score");
            Calendar last_training = Calendar.getInstance(); last_training.setTime(queryRes.getDate("last_training"));
            res.add(new Translation(ukr_id, eng_id, score, last_training));
        }
        queryRes.close();
        return res;
    }

    /**
     * This method could use ONLY for updates. If you want to add a new translation use Update.addWords(Connection conn, String[] ukr, String[] eng) method.
     */
    public static void saveTranslations(Statement stat, List<Translation> translations) throws  SQLException{
        var query = new StringBuilder("BEGIN;\n");
        for(var trans : translations){
            var str = String.format("UPDATE translation SET score=%d, last_training='%d-%d-%d' WHERE ukr_id=%d AND eng_id=%d;\n",
                    trans.score, trans.last_training.get(Calendar.YEAR), trans.last_training.get(Calendar.MONTH) + 1, trans.last_training.get(Calendar.DAY_OF_MONTH), trans.ukr_id, trans.eng_id);
            query.append(str);
        }
        query.append("COMMIT;");
        stat.executeUpdate(query.toString());
    }

    public static void updScoresToDate(Statement stmt)throws SQLException, IOException{
        //List<Translation> translations = loadTranslations(stmt);
        var today = Calendar.getInstance();
        var last_upd = Common.getLastUpd();
        int diff = (365 * (today.get(Calendar.YEAR) - last_upd.get(Calendar.YEAR))) + (today.get(Calendar.DAY_OF_YEAR) - last_upd.get(Calendar.DAY_OF_YEAR)); //на високосні похуй
        var sql = "UPDATE translation SET score = score - " + diff;
        stmt.executeUpdate(sql);
        ConfigFile.setParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.cfg", "last_upd",
                today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH)+1) + "-" + today.get(Calendar.YEAR));
        //for(var trans : translations)
        //    trans.addScore(-diff);
        //saveTranslations(stmt, translations);
    }

    public void addScore(int increase)throws IOException{
        score += increase;
        var today = Calendar.getInstance();
        if(increase <= 0)
            if(!Common.isToday(ConfigFile.getParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.cfg", "last_upd")))
                ConfigFile.setParam("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.cfg", "last_upd",
                today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH)+1) + "-" + today.get(Calendar.YEAR)); //decreasing only in updScoresToDate
        else             last_training = today;                                                                                     //increasing only while training
    }

    public static int getAvgScore(Statement stat) throws SQLException{
        var sql = "SELECT avg(score) FROM translation";
        var qRes = stat.executeQuery(sql);
        qRes.next();
        return (int)qRes.getDouble(1);
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
