package Dictionary.Menus;

import Dictionary.Auxiliary.RandomDistributor;
import Dictionary.Entities.Translation;
import Dictionary.TrainingStatement;

import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static Dictionary.Tables.WordTables.*;
import static Dictionary.Difficulty.*;
import static Dictionary.Program.history;

public enum MainMenuSelections implements AbstractMenu{
    StartTraining{
        public boolean action(Statement stat) throws Exception{
            List<Translation>
                    ukrToEng = new LinkedList<>(),
                    engToUkr = new LinkedList<>();
            var dist = new RandomDistributor(0.6, new Random());
            dist.distribute(Translation.loadTranslations(stat, "score <= (SELECT avg(score) FROM dictionary)"), ukrToEng, engToUkr);

            var engToUkrStat = new TrainingStatement(Hard, engToUkr, eng_words);
            engToUkr = engToUkrStat.start();

            var ukrToEngStat = new TrainingStatement(Hard, ukrToEng, ukr_words);
            ukrToEng = ukrToEngStat.start();

            engToUkrStat = new TrainingStatement(Medium, engToUkr, eng_words);
            engToUkr = engToUkrStat.start();

            ukrToEngStat = new TrainingStatement(Medium, ukrToEng, ukr_words);
            ukrToEng = ukrToEngStat.start();

            while(ukrToEng.size() != 0 && engToUkr.size() != 0){
                engToUkrStat = new TrainingStatement(Easy, engToUkr, eng_words);
                engToUkr = engToUkrStat.start();

                ukrToEngStat = new TrainingStatement(Easy, ukrToEng, ukr_words);
                ukrToEng = ukrToEngStat.start();
            }

            history.saveDailyScore(stat);
            return false;
        }
        public String toString() { return "Start training"; }
    },
    AddWords{
        public boolean action(Statement stat) throws Exception{
            MenuHandler.handle(UpdateDictionaryMenu.class, stat.getConnection());
            return false;
        }
        public String toString() { return "Update dictionary"; }
    },
    ShowDictionary{
        public boolean action(Statement stat) throws Exception{
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
            return false;
        }
        public String toString() { return "Show your dictionary"; }
    },
    Quit{
        public boolean action(Statement stat) throws Exception{
            stat.getConnection().close();
            return true;
        }
        public String toString() { return "Quit"; }
    };
}
