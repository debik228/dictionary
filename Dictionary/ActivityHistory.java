package Dictionary;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ActivityHistory {
    public HashMap<Date, Integer> history;
    private int dailyScore;
    private final boolean newNotation;
    private final Date today;

    public ActivityHistory(Statement stat) throws SQLException {
        history = new HashMap<>();
        var qRes = stat.executeQuery("SELECT * FROM activity_history");
        while(qRes.next())
            history.put(qRes.getDate("day"), qRes.getInt("score"));
        qRes = stat.executeQuery("SELECT * FROM CURRENT_DATE;");
        qRes.next();
        today = qRes.getDate(1);
        Integer dailyScore = history.get(today);
        if(dailyScore == null){
            this.dailyScore = 0;
            newNotation = true;
            history.put(today, dailyScore);
        }
        else {
            newNotation = false;
            this.dailyScore = history.get(today);
        }
    }

    public void increaseDailyScore(int increasing){
        if(increasing < 0)throw new IllegalArgumentException();
        dailyScore += increasing;
        history.put(today, dailyScore);
    }

    public void saveDailyScore(Statement stat) throws SQLException{
        String sql;
        var today = new Date();
        if(newNotation) {
            sql = String.format("INSERT INTO activity_history (score) VALUES (%d);", dailyScore);
            stat.execute(sql);
        }
        else{
            sql = String.format("UPDATE activity_history SET score = %d WHERE day = '%d-%d-%d';", dailyScore, 1900 + today.getYear(), today.getMonth() + 1, today.getDate());
            stat.executeUpdate(sql);
        }
    }

    public String toString() {
        //var sb = new StringBuilder("рот єбав того дауна, який зробив січень нульовим місяцем\n");
        //for(var key : history.keySet())
        //    sb.append(String.format("%d-%d-%d - %d\n", key.get(1), (key.get(2) + 1), key.get(5), history.get(key)));
        //return sb.toString();
        return history.toString();
    }
}
