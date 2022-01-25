package Dictionary.Menus;

import Dictionary.Auxiliary.RandomDistributor;
import Dictionary.Dialog.FloatDialog;
import Dictionary.Entities.Translation;
import Dictionary.Program;
import Dictionary.TableContentPrinter;
import Dictionary.Tables.Tables;
import Dictionary.TrainingStatement;

import java.util.*;

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
            var q = new FloatDialog("Which load quotient do you want?");
            var loadQuotient = q.getResult();
            Locale.setDefault(Locale.US);
            var query = String.format("score <= %f*(SELECT avg(score) FROM dictionary)", loadQuotient);
            dist.distribute(Translation.loadTranslations(query), ukrToEng, engToUkr);

            var engToUkrStat = new TrainingStatement(Hard, engToUkr, eng_words,1);
            engToUkr = engToUkrStat.start();

            var ukrToEngStat = new TrainingStatement(Hard, ukrToEng, ukr_words,1);
            ukrToEng = ukrToEngStat.start();

            engToUkrStat = new TrainingStatement(Medium, engToUkr, eng_words,2);
            engToUkr = engToUkrStat.start();

            ukrToEngStat = new TrainingStatement(Medium, ukrToEng, ukr_words,2);
            ukrToEng = ukrToEngStat.start();

            int round = 3;
            while(ukrToEng.size() != 0 || engToUkr.size() != 0){
                engToUkrStat = new TrainingStatement(Easy, engToUkr, eng_words, ++round);
                engToUkr = engToUkrStat.start();

                ukrToEngStat = new TrainingStatement(Easy, ukrToEng, ukr_words, round);
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
        private TableContentPrinter printer = new TableContentPrinter(Tables.dictionary, new TableContentPrinter.Column[]{
                    new TableContentPrinter.Column("ukr", "word", "%-25s"),
                    new TableContentPrinter.Column("ukr_id", "id", "%-6s"),
                    new TableContentPrinter.Column("score", "score", "%-5s"),
                    new TableContentPrinter.Column("eng_id", "id", "%-6s"),
                    new TableContentPrinter.Column("translate", "word", "%-25s"),
                    new TableContentPrinter.Column("last_training", "last trained", "%-10s"),
                    new TableContentPrinter.Column("successful_tryings", "successfully tryings", "%25s")}, "ORDER BY score DESC");
        public boolean action() throws Exception{
            System.out.println(printer.getTableContent());
            MenuHandler.handle(StatisticsMenu.class);
            return false;
        }
        public String toString() { return "Show dictionary statistics"; }
    },
    Quit{
        public boolean action() throws Exception{
            Program.dictionary.close();
            return true;
        }
        public String toString() { return "Quit"; }
    };
}
