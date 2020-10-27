package Dictionary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Statement;

public enum MainMenuSelections {
    StartTraining("Start training"){
        public void action(Statement stat) throws Exception{
            Training.train(stat);
        }
    },
    AddWords("Add words to dictionary"){
        public void action(Statement stat) throws Exception{
            var in = new BufferedReader(new InputStreamReader(System.in));
            var str = "";
            System.out.println("write translations in next format:\nбаняк, кастрюля = pot, pan\nPrint exit to leave");
            while (!str.matches("stop|exit")) {
                str = in.readLine();
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

            int i = 0;
            while (queryRes.next()) {
                var print = String.format("%-4s %-25s %-3d %-4d %-3d %-25s %10s", ++i + ".",
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
