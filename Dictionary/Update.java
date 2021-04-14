package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.Translation;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;
import Dictionary.Tables.Tables;
import Dictionary.Tables.WordTables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

public class Update {
    public static void addWords(Statement statement, String[] ukr, String[] eng) throws SQLException{
        int initScore = Math.min(Translation.getAvgScore(statement), 0);
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
        query = new StringBuilder("INSERT INTO translation (ukr_id, eng_id, score) VALUES ");
        for(var uWord : ukr){
            int uId = Common.getId(statement, Tables.ukr_words, uWord);
            for(var eWord : eng){
                int eId = Common.getId(statement, Tables.eng_words, eWord);
                query.append(String.format("(%d, %d, %d), ", uId, eId, initScore));
            }
        }
        query.setLength(query.length() - 2);
        query.append(";\nCOMMIT;");

        //executing last query
        System.out.println(query);
        statement.executeUpdate(query.toString());
    }

    public static <T extends Word> void updateWord(Connection conn, T newWord, T oldWord)throws SQLException{
        var stat = conn.createStatement();
        var sql = String.format("UPDATE %s SET word='%s', score=%s, pos='%s' WHERE word='%s'",
                (newWord instanceof EngWord)?"eng_words":"ukr_words", newWord.word.replaceAll("'", "''"), newWord.score, newWord.partOfSpeech.name(), oldWord.word);
        stat.executeUpdate(sql);
        stat.close();
    }

    public static void definePoS(Statement stat, List<? extends Word> wordList)throws SQLException{
        WordTables table = wordList.get(0).getClass()==UkrWord.class?WordTables.ukr_words:WordTables.eng_words;
        var in = new Scanner(System.in);
        var parts = Word.PoS.class.getEnumConstants();

        for(var word: wordList){
            System.out.println(firstCharacterToUpperCase(word.word) + " is a");
            printResponseVariants(parts);
            var response = in.nextLine().toLowerCase();
            var newPoS = parseUserResponse(response, parts);
            if(newPoS == null)break;
            var newWordValue = word.word;
            if(newPoS == Word.PoS.Verb && table == WordTables.eng_words) {
                System.out.println("Add \"to\" part? y/n");
                if(in.nextLine().toLowerCase().startsWith("y"))
                    newWordValue = "to " + newWordValue;
            }
            Word newWord = null;
            if(table == WordTables.eng_words)newWord = new EngWord(newWordValue, word.score, newPoS, word.regex);
            else                             newWord = new UkrWord(newWordValue, word.score, newPoS, word.regex);
            updateWord(stat.getConnection(), newWord, word);
        }
    }
    private static String firstCharacterToUpperCase(String str){
        return str.substring(0,1).toUpperCase() + str.substring(1);
    }
    private static void printResponseVariants(Word.PoS[] parts){
        for(int i = 0; i < 10; i++)
            System.out.println("\t(" + ((i+1)%10) + ")" + parts[i]);
        System.out.println("\t(i)Idiom");
        System.out.println("\t(u)Unknown");
        System.out.println("\t(q)Quit");
    }
    private static Word.PoS parseUserResponse(String response, Word.PoS[] parts){
        if(response.matches("[1-9]"))                                 return parts[Integer.parseInt(response) - 1];
        else if(response.matches("0"))                                return Word.PoS.PhrasalVerb;
        else{
            if(response.matches("qu?i?t?.*"))                         return null;
            else if(response.matches("no?u?n?.*"))                    return Word.PoS.Noun;
            else if(response.matches("ve?r?b?.*"))                    return Word.PoS.Verb;
            else if(response.matches("adje?c?t?i?v?e?.*"))            return Word.PoS.Adjective;
            else if(response.matches("adve?r?b?.*"))                  return Word.PoS.Adverb;
            else if(response.matches("pron?o?u?n?.*"))                return Word.PoS.Pronoun;
            else if(response.matches("prep?o?s?i?t?i?o?n?.*"))        return Word.PoS.Preposition;
            else if(response.matches("co?n?j?u?n?c?t?i?o?n?.*"))      return Word.PoS.Conjunction;
            else if(response.matches("int?e?r?j?e?c?t?i?o?n?.*"))     return Word.PoS.Interjection;
            else if(response.matches("art?i?c?l?e?.*"))               return Word.PoS.Article;
            else if(response.matches("phr?a?s?a?l? *v?e?r?b?.*"))     return Word.PoS.PhrasalVerb;
            else if(response.matches("id?i?o?m?.*"))                  return Word.PoS.Idiom;
            else if(response.matches("un?k?n?o?w?n?.*"))              return Word.PoS.Unknown;
            else{ System.out.println("Unknown answer.");                    return Word.PoS.Unknown;
            }
        }
    }

    public static void defineRegex(Statement stat, int wordID, String regex)throws SQLException{
        var regexTable = regex.matches(".*[A-Za-z]+.*") ? Tables.eng_regex:Tables.ukr_regex;
        if(Common.contains(stat, regexTable, "word_id = " + wordID))
            stat.executeUpdate(String.format("UPDATE %s SET regex = '%s' WHERE word_id = %d", regexTable, regex.replaceAll("'", "''"), wordID));
        else
            stat.execute(String.format("INSERT INTO %s (word_id, regex) VALUES (%d, '%s')", regexTable, wordID, regex.replaceAll("'", "''")));
    }
}
