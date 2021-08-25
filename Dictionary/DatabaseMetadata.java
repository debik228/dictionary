package Dictionary;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMetadata {
    private static Number getAggregateScoreFuncResult(String funcName){
        {
            Statement stat = Program.dictionary.getStatement();
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
        return (int)getAggregateScoreFuncResult("avg");
    }
    public static int getMaxScore(){
        return (int)getAggregateScoreFuncResult("max");
    }
    public static int getMinScore(){
        return (int)getAggregateScoreFuncResult("min");
    }

    public static int numWordsToTrain(){
        var stat = Program.dictionary.getStatement();
        int numWordsToTrain;
        try {
            var resultSet = stat.executeQuery("SELECT count(*) FROM dictionary  WHERE score <= " + getAvgScore());
            resultSet.next();
            numWordsToTrain = resultSet.getInt(1);
            stat.close();
            return numWordsToTrain;
        }catch (SQLException хуй){
            System.out.println("Пососи");
        }
        throw new RuntimeException("Пососи exception");
    }
}
