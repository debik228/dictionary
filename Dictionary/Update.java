package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

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

    public static void updateWord(Connection conn, int id, Word word)throws SQLException{
        var stat = conn.createStatement();
        var sql = String.format("UPDATE %s SET word='%s', score=%s, pos='%s' WHERE id = %d", (word instanceof EngWord)?"eng_words":"ukr_words", word.word.replaceAll("'", "''"), word.score, word.partOfSpeech.name(), id);
        stat.executeUpdate(sql);
        stat.close();
    }

    public static void definePoS(Statement stat, Tables table) throws SQLException{
        if(table!= Tables.eng_words && table!=Tables.ukr_words)
            throw new IllegalArgumentException();
        var wordTable = Common.loadWordTable(stat, table, "pos = 'Unknown'");
        String answer = null;
        var in = new Scanner(System.in);
        var parts = Word.PoS.class.getEnumConstants();

        for(var key: wordTable.keySet()){
            var word = wordTable.get(key);
            System.out.println(word.word.substring(0,1).toUpperCase() + word.word.substring(1) + " is a");
            for(int i = 0; i < 10; i++)
                System.out.println("\t(" + ((i+1)%10) + ")" + parts[i]);
            System.out.println("\t(i)Idiom");
            System.out.println("\t(u)Unknown");
            System.out.println("\t(q)Quit");
            answer = in.nextLine().toLowerCase();
            Word.PoS newPoS = null;
            String newWord = word.word;
            if(answer.matches("[1-9]"))
                newPoS = parts[Integer.parseInt(answer) - 1];
            else if(answer.matches("0"))
                newPoS = Word.PoS.PhrasalVerb;
            else{
                if(answer.matches("qu?i?t?.*"))                         break;
                else if(answer.matches("no?u?n?.*"))                    newPoS = Word.PoS.Noun;
                else if(answer.matches("ve?r?b?.*"))                    newPoS = Word.PoS.Verb;
                else if(answer.matches("adje?c?t?i?v?e?.*"))            newPoS = Word.PoS.Adjective;
                else if(answer.matches("adve?r?b?.*"))                  newPoS = Word.PoS.Adverb;
                else if(answer.matches("pron?o?u?n?.*"))                newPoS = Word.PoS.Pronoun;
                else if(answer.matches("prep?o?s?i?t?i?o?n?.*"))        newPoS = Word.PoS.Preposition;
                else if(answer.matches("co?n?j?u?n?c?t?i?o?n?.*"))      newPoS = Word.PoS.Conjunction;
                else if(answer.matches("int?e?r?j?e?c?t?i?o?n?.*"))     newPoS = Word.PoS.Interjection;
                else if(answer.matches("art?i?c?l?e?.*"))               newPoS = Word.PoS.Article;
                else if(answer.matches("phr?a?s?a?l? *v?e?r?b?.*"))     newPoS = Word.PoS.PhrasalVerb;
                else if(answer.matches("id?i?o?m?.*"))                  newPoS = Word.PoS.Idiom;
                else if(answer.matches("un?k?n?o?w?n?.*"))              newPoS = Word.PoS.Unknown;
                else{
                    System.out.println("Unknown answer.");
                    newPoS = Word.PoS.Unknown;
                }
            }
            if(newPoS == Word.PoS.Verb && table == Tables.eng_words) newWord = "to " + newWord;
            Word updatedWord = null;
            if(table == Tables.eng_words)updatedWord = new EngWord(newWord, word.score, newPoS);
            else                         updatedWord = new UkrWord(newWord, word.score, newPoS);
            updateWord(stat.getConnection(), key, updatedWord);
        }
    }
}
