package Dictionary.PlotBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DictionaryPlotDataGenerator {
    public static final String dbName = "dictionary";
    public static final String username = "postgres";
    public static final String password = "123456789";
    public static Connection conn;

    private static Statement getStat(){
        Statement stat = null;
        try {
            if(conn == null) {
                    conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, username, password);
            }
            stat = conn.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1488);
        }
        return stat;
    }
    private static int getAggregateScoreFuncResult(String funcName){
        {
            Statement stat = getStat();
            int res = -1488;
            try {
                var queryRes = stat.executeQuery("SELECT " + funcName + "(score) FROM dictionary");
                queryRes.next();
                res = queryRes.getInt(1);
                stat.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.exit(1488);
            }
            return res;
        }
    }
    public static int getAvgScore(){
        return getAggregateScoreFuncResult("avg");
    }
    public static int getMaxScore(){
        return getAggregateScoreFuncResult("max");
    }
    public static int getMinScore(){
        return getAggregateScoreFuncResult("min");
    }
    public static int getMaxCountOfTuplesWithSameScore(){
        Statement stat = getStat();
        int max = -1488;
        try {
            var queryRes = stat.executeQuery("SELECT max(qwerty) FROM(\n" +
                    "\tSELECT score, count(score) AS qwerty FROM dictionary GROUP BY score\n" +
                    ") AS derivedTable;");
            queryRes.next();
            max = queryRes.getInt(1);
            stat.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1488);
        }
        return max;
    }

    /**
     * @return bias between array position and score
     */
    public static int getBias(){
        int min = getMinScore();
        return min < 0?-1 * min:0;
    }

    public static int[] getScoreDistribution(){
        Statement stat = getStat();
        int min = getMinScore();
        int max = getMaxScore();
        int bias = getBias();
        int[] plotData;
        if(min<0)
            plotData = new int[max - min + 1];
        else
            plotData = new int[max + 1];

        try {
            //var queryRes = stat.executeQuery("SELECT score, count(score) FROM dictionary WHERE score > 0 GROUP BY score ORDER BY score;");
            var queryRes = stat.executeQuery("SELECT score, count(score) FROM dictionary GROUP BY score ORDER BY score;");
            while (queryRes.next())
                plotData[queryRes.getInt(1) + bias] = queryRes.getInt(2);
            stat.close();
        }catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1488);
        }
        return plotData;
    }
}
