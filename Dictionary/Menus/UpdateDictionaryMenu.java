package Dictionary.Menus;

import Dictionary.Common;
import Dictionary.Tables;
import Dictionary.Update;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static Dictionary.Tables.*;

public enum UpdateDictionaryMenu implements AbstractMenu {
    AddWords{
        public boolean action(Statement stat) throws Exception{
            var in = new Scanner(System.in, StandardCharsets.UTF_8);
            var str = "";
            System.out.println("write translations in next format:\nбаняк, кастрюля = pot, pan\nPrint exit to leave");
            while (!str.matches("stop|exit")) {
                str = in.nextLine();
                if (str.matches("[^=]+=[^=]+")) {
                    var wordRoster = str.split(" *= *");
                    var ukr = wordRoster[0].split(", *");
                    var eng = wordRoster[1].split(", *");

                    for(int i = 0; i < ukr.length; i++) ukr[i] = ukr[i].replaceAll("'", "''");
                    for(int i = 0; i < eng.length; i++) eng[i] = eng[i].replaceAll("'", "''");

                    Update.addWords(stat, ukr, eng);
                }
            }
            return false;
        }
        public String toString() { return "Add Words"; }
    },
    DefinePoS {
        public boolean action(Statement stat)throws SQLException {
            System.out.println("Which table you want first?\n1. ukr_words\n2. eng_words");
            var response = new Scanner(System.in).nextLine();
            if(response.charAt(0) == '1') {
                Update.definePoS(stat, ukr_words);
                Update.definePoS(stat, eng_words);
            }
            else{
                Update.definePoS(stat, eng_words);
                Update.definePoS(stat, ukr_words);
            }
            return false;
        }
        public String toString() { return "Define part of speech"; }
    },
    AddRegex{
        public boolean action(Statement stat)throws IOException, SQLException {
            Tables loadingWordTable = null;
            Tables loadingRegexTable = null;
            String answer;
            var in = new Scanner(System.in, StandardCharsets.UTF_8);
            do {
                System.out.println("For which table you want to add regex\n1. ukr_words\n2. eng_words");
                answer = in.nextLine().toLowerCase();
                if(     answer.startsWith("1") || answer.startsWith("ukr")) {
                    loadingWordTable = ukr_words;
                    loadingRegexTable= ukr_regex;
                }
                else if(answer.startsWith("2") || answer.startsWith("eng")){
                    loadingWordTable = eng_words;
                    loadingRegexTable= eng_regex;
                }
            }while(loadingWordTable == null);
            var words = Common.loadWordTable(stat, loadingWordTable);
            var regexes = Common.loadRegexTable(stat, loadingRegexTable);
            System.out.println("For which word do you want to define regex. Type back to return");
            for(var id:words.keySet())
                System.out.println(id + ". " + words.get(id).word);
            do{
                answer = in.nextLine();
                if(answer.equals(("back"))) return false;
                else {
                    try {
                        int id = -1;
                        if(answer.matches("\\d*"))
                            id = Integer.parseInt(answer);
                        else
                            for(var currId:words.keySet())
                                if(words.get(currId).word.equals(answer)){
                                    id = currId;
                                    break;
                                }
                        var currRegex = regexes.get(id);
                        if (currRegex != null)
                            System.out.println("Regex for " + words.get(id).word + " word already exists: '" + currRegex + "'. It will be replaced with new regex with you input.");
                        else
                            System.out.println("Print a regex for " + words.get(id).word);
                        var newRegex = in.nextLine();
                        regexes.put(id, newRegex);
                        Update.defineRegex(stat, id, newRegex);
                    }catch (NullPointerException e){ System.out.println("There is no word with such id. Please input correct number."); }
                }
                System.out.println("Print a number of word for which you want to add a regex. Type back to return");
            }while(true);
        }
        public String toString() {return "Add regex";}
    },
    Back{
        public boolean action(Statement stat){ return true;}
        public String toString() { return "Back"; }
    };
}
