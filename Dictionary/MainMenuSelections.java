package Dictionary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.Scanner;

public enum MainMenuSelections {
    StartTraining("Start training"){
        public void action(Statement stat) throws Exception{
            Training.train(stat);
        }
    },
    AddWords("Add words to dictionary"){
        public void action(Statement stat) throws Exception{
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
        }
    },
    ShowDictionary("Show your dictionary"){
        public void action(Statement stat) throws Exception{
            var query = "SELECT * FROM dictionary ORDER BY score ASC";
            var queryRes = stat.executeQuery(query);

            System.out.printf("%-4s %-25s %-6s %-5s %-6s %-25s %-10s\n", "num", "word", "id", "score", "id", "word", "last trained");

            int i = 0;
            while (queryRes.next()) {
                var print = String.format("%-4s %-25s %-6s %-5s %-6s %-25s %10s", ++i + ".",
                        queryRes.getString(1),
                        queryRes.getInt(2),
                        queryRes.getInt(3),
                        queryRes.getInt(4),
                        queryRes.getString(5),
                        queryRes.getDate(6));
                System.out.println(print);
            }
        }
    },
    Quit("Quit"){
        public void action(Statement stat) throws Exception{
            stat.getConnection().close();
        }
    };

    public final String name;
    MainMenuSelections(String name){this.name = name;}
    public abstract void action(Statement stat) throws Exception;
}
