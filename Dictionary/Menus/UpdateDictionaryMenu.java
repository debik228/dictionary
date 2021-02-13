package Dictionary.Menus;

import Dictionary.Tables;
import Dictionary.Update;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

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
                Update.definePoS(stat, Tables.ukr_words);
                Update.definePoS(stat, Tables.eng_words);
            }
            else{
                Update.definePoS(stat, Tables.eng_words);
                Update.definePoS(stat, Tables.ukr_words);
            }
            return false;
        }
        public String toString() { return "Define part of speech"; }
    },
    Back{
        public boolean action(Statement stat){ return true;}
        public String toString() { return "Back"; }
    };
}
