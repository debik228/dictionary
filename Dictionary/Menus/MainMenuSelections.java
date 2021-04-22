package Dictionary.Menus;

import Dictionary.Auxiliary.RandomDistributor;
import Dictionary.Entities.Translation;
import Dictionary.Program;
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
        public boolean action() throws Exception{
            List<Translation>
                    ukrToEng = new LinkedList<>(),
                    engToUkr = new LinkedList<>();
            var dist = new RandomDistributor(0.3, new Random());
            dist.distribute(Translation.loadTranslations("score <= (SELECT avg(score) FROM dictionary)"), ukrToEng, engToUkr);

            var engToUkrStat = new TrainingStatement(Hard, engToUkr, eng_words);
            engToUkr = engToUkrStat.start();

            var ukrToEngStat = new TrainingStatement(Hard, ukrToEng, ukr_words);
            ukrToEng = ukrToEngStat.start();

            engToUkrStat = new TrainingStatement(Medium, engToUkr, eng_words);
            engToUkr = engToUkrStat.start();

            ukrToEngStat = new TrainingStatement(Medium, ukrToEng, ukr_words);
            ukrToEng = ukrToEngStat.start();

            while(ukrToEng.size() != 0 || engToUkr.size() != 0){
                engToUkrStat = new TrainingStatement(Easy, engToUkr, eng_words);
                engToUkr = engToUkrStat.start();

                ukrToEngStat = new TrainingStatement(Easy, ukrToEng, ukr_words);
                ukrToEng = ukrToEngStat.start();
            }

            history.saveDailyScore();
            return false;
        }
        public String toString() { return "Start training"; }
    },
    AddWords{
        public boolean action() throws Exception{
            MenuHandler.handle(UpdateDictionaryMenu.class);
            return false;
        }
        public String toString() { return "Update dictionary"; }
    },
    ShowDictionary{
        public boolean action() throws Exception{
            var query = "SELECT * FROM dictionary ORDER BY score ASC";
            var stat = Program.dictionary.getStatement();
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
            stat.close();
            return false;
        }
        public String toString() { return "Show your dictionary"; }
    },
    Quit{
        public boolean action() throws Exception{
            Program.dictionary.close();
            return true;
        }
        public String toString() { return "Quit"; }
    };
}
