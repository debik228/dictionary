package Dictionary.Menus;

import Dictionary.Training;

import java.sql.Statement;

public enum MainMenuSelections implements AbstractMenu{
    StartTraining{
        public boolean action(Statement stat) throws Exception{
            Training.train(stat);
            return false;
        }
        public String toString() { return "Start training"; }
    },
    AddWords{
        public boolean action(Statement stat) throws Exception{
            MenuHandler.handle(UpdateDictionary.class, stat.getConnection());
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
