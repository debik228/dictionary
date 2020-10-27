package Dictionary;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Update {
    public static void addWords(Statement statement, String[] ukr, String[] eng) throws SQLException{
        var query = new StringBuilder("BEGIN;\nINSERT INTO ukr_words (word) VALUES ");

        //first query(ukr_words)
        for (var uWord : ukr)
            if(!Common.contains(statement, Tables.ukr_words, "word = '" + uWord + "'"))
                query.append("('" + uWord + "'), ");
        query.setLength(query.length() - 2);
        query.append(";\n");
        if(query.length() == "BEGIN;\nINSERT INTO ukr_words (word) VALUES ".length())query.setLength(7);                //if no one ukrainian word can be added clear query

        //second query(eng_words)
        query.append("INSERT INTO eng_words (word) VALUES ");
        for (var eWord : eng)
            if(!Common.contains(statement, Tables.eng_words, "word = '" + eWord + "'"))
                query.append("('" + eWord + "'), ");
        query.setLength(query.length() - 2);
        if(query.toString().endsWith("INSERT INTO eng_words (word) VALUE"))query.setLength(query.length() - "INSERT INTO eng_words (word) VALUE".length());

        //executing first and second query
        System.out.println(query);
        statement.executeUpdate(query.toString());

        //last query(mtm connections)
        query = new StringBuilder("INSERT INTO translation (ukr_id, eng_id) VALUES ");
        for(var uWord : ukr){
            int uId = Common.getId(statement, Tables.ukr_words, uWord);
            for(var eWord : eng){
                int eId = Common.getId(statement, Tables.eng_words, eWord);
                query.append(String.format("(%d, %d), ", uId, eId));
            }
        }
        query.setLength(query.length() - 2);
        query.append(";\nCOMMIT;");

        //executing last query
        System.out.println(query);
        statement.executeUpdate(query.toString());
    }
}
