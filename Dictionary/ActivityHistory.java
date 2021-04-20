package Dictionary;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

public class ActivityHistory {
    public HashMap<Date, Integer> history;
    private int dailyScore;
    private boolean newNotation;
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

    public void increaseDailyScore(int increasing, Statement stat)throws SQLException{
        if(increasing < 0)throw new IllegalArgumentException();
        dailyScore += increasing;
        saveDailyScore(stat);
        history.put(today, dailyScore);
    }

    public void saveDailyScore(Statement stat) throws SQLException{
        String sql;
        if(newNotation) {
            sql = String.format("INSERT INTO activity_history (score) VALUES (%d);", dailyScore);
            stat.execute(sql);
            newNotation = false;
        }
        else{
            sql = String.format("UPDATE activity_history SET score = %d WHERE day = '%d-%d-%d';", dailyScore, 1900 + today.getYear(), today.getMonth() + 1, today.getDate());
            stat.executeUpdate(sql);
        }
    }

    public String toString() {
        return history.toString();
    }
}
