package Dictionary.Menus;

import Dictionary.Common;
import Dictionary.Program;
import Dictionary.Tables.RegexTables;
import Dictionary.Tables.WordTables;
import Dictionary.Update.Update;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

import static Dictionary.Tables.WordTables.*;
import static Dictionary.Tables.RegexTables.*;

public enum UpdateDictionaryMenu implements AbstractMenu {
    AddWords{
        public boolean action() throws Exception{
            var in = new Scanner(System.in, StandardCharsets.UTF_8);
            var str = "";
            System.out.println("write translations in next format:\nбаняк, кастрюля = pot, pan\nPrint exit to leave");
            while (!str.matches("stop|exit")) {
                str = in.nextLine();
                if (str.matches("[^=]+=[^=]+")) {
                    var wordRoster = str.split(" *= *");
                    var ukr = wordRoster[0].split(", *");
                    var eng = wordRoster[1].split(", *");

                    for(int i = 0; i < ukr.length; i++) ukr[i] = ukr[i].replaceAll("'", "''");//TODO: зробити таку заміну у всіх запитах до бд
                    for(int i = 0; i < eng.length; i++) eng[i] = eng[i].replaceAll("'", "''");

                    Update.addWords(ukr, eng);
                }
            }
            return false;
        }
        public String toString() { return "Add Words"; }
    },
    DefinePoS {
        public boolean action()throws SQLException {
            var stat = Program.dictionary.getStatement();
            var ukrWordList = Common.loadWordList(ukr_words, "pos = 'Unknown' ORDER BY last_upd DESC");
            var engWordList = Common.loadWordList(eng_words, "pos = 'Unknown' ORDER BY last_upd DESC");
            System.out.format("There is %d words with undefined PoS in ukr_words and %d in eng_words.\nWhich table you want first?\n1. ukr_words\n2. eng_words\n", ukrWordList.size(), engWordList.size());
            var response = new Scanner(System.in).nextLine();
            if(response.charAt(0) == '1') {
                Update.definePoS(stat, ukrWordList);
                Update.definePoS(stat, engWordList);
            }
            else{
                Update.definePoS(stat, engWordList);
                Update.definePoS(stat, ukrWordList);
            }
            stat.close();
            return false;
        }
        public String toString() { return "Define part of speech"; }
    },
    AddRegex{
        private String errorMessage;

        public boolean action()throws SQLException {
            WordTables loadingWordTable = null;
            RegexTables loadingRegexTable = null;
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
                else System.out.println("Unknown answer");
            }while(loadingWordTable == null);
            var words = Common.loadWordMap(loadingWordTable);
            var regexes = Common.loadRegexTable(loadingRegexTable);
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
                            System.out.println("Regex for " + words.get(id).word + " word already exists: '" + currRegex + "'. New regex will replace existing.");
                        else
                            System.out.println("Print a regex for " + words.get(id).word);
                        var newRegex = in.nextLine();
                        if(validateRegex(newRegex)) {
                            regexes.put(id, newRegex);
                            Update.defineRegex(id, newRegex);
                        }
                        else
                            System.out.println(errorMessage);
                    }catch (NullPointerException e){ System.out.println("There is no word with such id. Please input correct number."); }
                }
                System.out.println("Print a number of word for which you want to add a regex. Type back to return");
            }while(true);
        }
        private boolean validateRegex(String regex){
            try{
                "".matches(regex);
                return true;
            }catch (PatternSyntaxException e){
                errorMessage = "Regex " + e.getPattern() + " is invalid.\n" + e.getDescription() + " at index " + e.getIndex();
                return false;
            }
        }
        public String toString() {return "Add regex";}
    },
    Back{
        public boolean action(){ return true;}
        public String toString() { return "Back"; }
    };
}
