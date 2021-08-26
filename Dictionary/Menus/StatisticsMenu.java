package Dictionary.Menus;

import Dictionary.DatabaseMetadata;
import Dictionary.PlotBuilder.Main;
import Dictionary.TableContentPrinter;
import Dictionary.Tables.Tables;

public enum StatisticsMenu implements AbstractMenu {
    ShowStatistics{
        public boolean action() throws Exception {
            int avg = DatabaseMetadata.getAvgScore(),
                min = DatabaseMetadata.getMinScore(),
                max = DatabaseMetadata.getMaxScore(),
                num = DatabaseMetadata.numWordsToTrain();
            System.out.println("avg = " + avg);
            System.out.println("min = " + min);
            System.out.println("max = " + max);
            System.out.println("num = " + num);
            return false;
        }
        public String toString() {
            return "Show dictionary statistics";
        }
    },
    ShowDailyActivity{
        private TableContentPrinter printer = new TableContentPrinter(Tables.activity_history, new TableContentPrinter.Column[]{
                new TableContentPrinter.Column("score", "score", "%-6s"),
                new TableContentPrinter.Column("day", "date", "%10s")},
                "ORDER BY day");
        public boolean action() throws Exception {
            System.out.println(printer.getTableContent());
            return false;
        }
        public String toString() {
            return "Show daily activity";
        }
    },
    ShowWordAdditionByDate{
        private TableContentPrinter printer = new TableContentPrinter(Tables.addition_dates, new TableContentPrinter.Column[]{
                new TableContentPrinter.Column("addition_date", "date", "%-10s"),
                new TableContentPrinter.Column("count(*)", "new pairs", "%10s")},
                "GROUP BY addition_date ORDER BY addition_date");
        public boolean action() throws Exception {
            System.out.println(printer.getTableContent());
            return false;
        }
        public String toString() {
            return "Show word addition by date";
        }
    },
    RenderScoreDistributionPlot{
        public boolean action() throws Exception {
            System.out.println("Rendering plot...");
            String plotName = Main.render();
            System.out.println("Plot rendered successfully. Saved as: " + plotName);
            return false;
        }
        public String toString() {
            return "Render score distribution plot";
        }
    },
    Back{
        public boolean action() throws Exception {
            return true;
        }
        public String toString() {
            return "Back";
        }
    }
}
