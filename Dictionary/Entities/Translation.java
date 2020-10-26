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
    public Calendar last_upd;

    public Translation(int ukr_id, int eng_id, int score, Calendar last_upd){
        this.ukr_id = ukr_id;
        this.eng_id = eng_id;
        this.score = score;
        this.last_upd = last_upd;
    }

    public static ArrayList<Translation> loadTranslations(Statement stat) throws SQLException{
        return loadTranslations(stat, "");
    }

    public static ArrayList<Translation> loadTranslations(Statement stat, String condition) throws SQLException{
        var res = new ArrayList<Translation>();
        ResultSet queryRes = null;
        String query = "SELECT ukr_id, eng_id, score, last_upd FROM translation" + (condition.length() == 0 ? "" : " WHERE " + condition) + " ORDER BY score ASC;";
        try {
            queryRes = stat.executeQuery(query);
        }catch (SQLException e){
            throw new RuntimeException("SQL exception was occurred. Query: " + query, e);
        }
        while(queryRes.next()){
            int ukr_id = queryRes.getInt("ukr_id");
            int eng_id = queryRes.getInt("eng_id");
            int score = queryRes.getInt("score");
            Calendar last_upd = Calendar.getInstance(); last_upd.setTime(queryRes.getDate("last_upd"));
            res.add(new Translation(ukr_id, eng_id, score, last_upd));
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
            var str = String.format("UPDATE translation SET score=%d, last_upd='%d-%d-%d' WHERE ukr_id=%d AND eng_id=%d;\n", trans.score, trans.last_upd.get(Calendar.YEAR), trans.last_upd.get(Calendar.MONTH) + 1, trans.last_upd.get(Calendar.DAY_OF_MONTH), trans.ukr_id, trans.eng_id);
            query.append(str);
        }
        query.append("COMMIT;");
        //System.out.println(query);
        stat.executeUpdate(query.toString());
    }

    public static void updScoresToDate(Statement stmt)throws SQLException, IOException {
        //if score have updated today - return
        var user = new ConfigFile("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.txt");
        var lastTrainingDate = user.params.get("last_training");
        if(Common.isToday(lastTrainingDate)) return;

        List<Translation> translations = loadTranslations(stmt);
        var today = Calendar.getInstance();
        for(var trans : translations){
            var last_upd = trans.last_upd;
            //last_upd.set(Calendar.MONTH, last_upd.get(Calendar.MONTH) + 1);
            long diff = (365 * (today.get(Calendar.YEAR) - last_upd.get(Calendar.YEAR))) + (today.get(Calendar.DAY_OF_YEAR) - last_upd.get(Calendar.DAY_OF_YEAR)); //на високосні похуй
            trans.score -= diff;
        }
        saveTranslations(stmt, translations);
    }

    public void addScore(int increase){
        score += increase;
        last_upd = Calendar.getInstance();
    }

    public String toString(){
        return ukr_id + ", " + eng_id + ", " + score + ", " + last_upd.get(Calendar.YEAR) + '-' + (last_upd.get(Calendar.MONTH)+1) + '-' + last_upd.get(Calendar.DAY_OF_MONTH);
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
