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
            Update.definePoS(stat, Tables.eng_words);
            Update.definePoS(stat, Tables.ukr_words);
            return false;
        }
        public String toString() { return "Define part of speech"; }
    },
    Back{
        public boolean action(Statement stat){ return true;}
        public String toString() { return "Back"; }
    };
}
