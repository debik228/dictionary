package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Translation;
import Dictionary.Entities.Word;
import Dictionary.Tables.WordTables;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static Dictionary.Common.sameDate;
import static Dictionary.Difficulty.*;
import static java.lang.Character.isLetter;

public class TrainingStatement {
    private static final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

    private final Difficulty difficulty;
    private final List<Translation> translations;
    private final WordTables translatingFrom;

    private int questionsTot;
    private int questionsNum = 1;

    public TrainingStatement(Difficulty difficulty, List<Translation> translations, WordTables translatingFrom){
        this.difficulty = difficulty;
        this.translations = translations;
        this.translatingFrom = translatingFrom;
        this.questionsTot = translations.size();
    }

    public List<Translation> start()throws SQLException, IOException{
        var conn = Program.dictionary.getConn();
        var stmt = conn.createStatement();             //TODO зробити в database метод, що повертав би statement і позабирати нахуй подібну хуйню звідусіль

        Translation currTrans = null;
        var nextTour = new LinkedList<Translation>();
        while(translations.size() != 0){
            currTrans = translations.get(0);
            askForTranslation(currTrans);
            HashSet<String> response = getResponse();
            List<Translation> translationVariants = Translation.loadTranslations(stmt,
                    (translatingFrom == WordTables.ukr_words ?"ukr_id = ":"eng_id = ") + currTrans.getWordId(translatingFrom));

            var checkingResult = checkResponse(response, translationVariants, translatingFrom);
            LinkedList<Translation>  rightResponses       = checkingResult.getRightResponses();
            LinkedList<Translation>  nonUsedTranslations  = checkingResult.getNonUsedTranslations();

            checkingResult.addScore(stmt, difficulty.getStandardAward());
            translations.removeAll(rightResponses);

            if(rightResponses.isEmpty()){
                translations.remove(currTrans);
                nextTour.add(currTrans);
            }
            checkingResult.printResults(translationVariants, translatingFrom);

            translations.removeAll(nonUsedTranslations);//we've show all the variants, so shouldn't give a chance to give a right response in this tour
            System.out.println();
            questionsNum++;
        }
        return nextTour;
    }

    private void askForTranslation(Translation currTrans){
        System.out.println("\u001B[37m" + questionsNum + "/" + questionsTot + " Як перекладаєцця " + currTrans.getWord(translatingFrom) + "\u001B[0m");
    }

    private HashSet<String> getResponse(){
        var response = new HashSet<String>();
        var responseString = in.nextLine().toLowerCase();
        Collections.addAll(response, responseString.split(", *"));
        return response;
    }

    private QuestionResults checkResponse(HashSet<String> responses, List<Translation> translationsVariants, WordTables translatingFrom){
        var rightResponses = new LinkedList<Translation>();
        var wrongResponses = new LinkedList<String>();
        var nonUsedTranslations = new LinkedList<>(translationsVariants);
        var typos = new HashMap<String, String>();

        for(String response : responses){
            var wrong = true;
            for(var currTransVariant : translationsVariants) {
                WordTables translatingTo = translatingFrom.getOpposite();
                Word checkedScope = currTransVariant.getWord(translatingTo);
                String modified = modifyString(difficulty, checkedScope);
                if(response.matches(modified)){
                    rightResponses.add(currTransVariant);
                    nonUsedTranslations.remove(currTransVariant);
                    wrong = false;
                    if(difficulty != Hard && !response.matches(modifyString(Hard, checkedScope)))//In fact hard difficulty, don't allow mistakes, but allows typos. So mistake list always empty in this level.
                        typos.put(response, checkedScope.word.toLowerCase());
                    break;
                }
            }
            if(wrong)wrongResponses.add(response);
        }

        return new QuestionResults(rightResponses, wrongResponses, nonUsedTranslations, typos);
    }

    public static String modifyString(Difficulty difficulty, Word checkedScope){
        var str = checkedScope.regex;//.toLowerCase();
        var res = str;//str is an original word. Please don't modify it.
        var sb = new StringBuilder();
        String regex;

        res = res.replaceAll("с[ья]", "с[ья]");

        //unnecessary 'to' before verbs
        if(checkedScope.getClass() == EngWord.class && checkedScope.partOfSpeech == Word.PoS.Verb) {
            if (str.startsWith("to "))
                res = res.replaceFirst("to ", "(to )?");
            else
                res = "(to )?" + res;
            //res = res.substring(res.indexOf(str.charAt("to ".length()))); //doesnt word with words which starts with 'o' (e.g. to offer)
            //res = "(to )?" + res;
        }

        //any ending variants in ukr adj
        if(checkedScope.getClass() == UkrWord.class && checkedScope.partOfSpeech == Word.PoS.Adjective){
            regex = "(ий|а|е|і)";
            //if(str.endsWith("ий") || str.endsWith("а") || str.endsWith("е") || str.endsWith("і")){    //works same, PoS in db useless//no words like уява shouldn't modify, but it does
            //res = res.replaceAll(regex+"$", regex);//doesn't work
            if(str.endsWith("ий"))res = res.substring(0, res.lastIndexOf('и'));
            if(str.endsWith("а")) res = res.substring(0, res.lastIndexOf('а'));
            if(str.endsWith("е")) res = res.substring(0, res.lastIndexOf('е'));
            if(str.endsWith("і")) res = res.substring(0, res.lastIndexOf('і'));
            res = res + regex;
        }

        //ignore some non-word-characters
        sb.setLength(0);
        regex = "[\\s.,']";                        //boundary characters, apostrophe, comma and dot. Another non-word characters don't include because it will spoil regular expressions
        var words = res.split(regex + "+");  //previously used [^а-яА-Я^\w]
        for(var word : words){
            sb.append(word);
            sb.append(regex + "*");
        }
        res = sb.toString();

        switch (difficulty) {
            case Easy:
                //res = res.replaceAll("[ck]", "[ck]");
            case Medium:
                //allow any word-character duplication
                sb.setLength(0);
                Character prevChar = null;
                for (var ch : res.toCharArray()) {
                    if(prevChar != null && prevChar == ch && isLetter(ch))continue;
                    sb.append(ch);
                    prevChar = ch;
                    if(isLetter(ch))
                        sb.append('+');
                }
                res = sb.toString();
        }
        return res;
    }

    //TODO доробити методи і застосувати їх в checkResponse
    /**
     * @param gained рядок, що перевіряється
     * @param expected очікувана відповідь
     * @return true, якщо якщо gained відрізняється від expected не більше ніж на otherCharsNumber символів і false в іншому випадку
     */
    public static boolean sameExceptOf(int otherCharsNumber, String gained, String expected){
        return false;
    }

    private static class QuestionResults {
        private static String RED_TEXT = "\u001B[31m";
        private static String GREEN_TEXT = "\u001B[32m";
        private static String YELLOW_TEXT = "\u001B[33m";
        private static String BLUE_TEXT = "\u001B[34m";
        private static String RESET = "\u001B[0m";

        private final LinkedList<Translation> rightResponses;
        private final LinkedList<String>      wrongResponses;
        private final LinkedList<Translation> nonUsedTranslations;
        private final HashMap<String, String> typos;

        public QuestionResults(LinkedList<Translation> rightResponses, LinkedList<String> wrongResponses, LinkedList<Translation> nonUsedTranslations, HashMap<String, String> typos){
            this.rightResponses = rightResponses;
            this.wrongResponses = wrongResponses;
            this.nonUsedTranslations = nonUsedTranslations;
            this.typos = typos;
        }

        public LinkedList<Translation> getRightResponses() { return rightResponses; }
        public LinkedList<String> getWrongResponses() { return wrongResponses; }
        public LinkedList<Translation> getNonUsedTranslations() { return nonUsedTranslations; }
        public HashMap<String, String> getTypos() { return typos; }

        public static void turnOffColorization(){
            RED_TEXT = GREEN_TEXT = YELLOW_TEXT = BLUE_TEXT = RESET = "";
        }

        public void addScore(Statement stmt, int award)throws IOException, SQLException {
            for(var trans : rightResponses) {
                if(!sameDate(trans.last_training, Calendar.getInstance())){
                    trans.addScore(award);
                    Program.history.increaseDailyScore(award, stmt);
                }
                else {
                    trans.addScore(1);
                    Program.history.increaseDailyScore(1, stmt);
                }
                trans.last_training.setTime(new Date());
            }
            Translation.updTranslations(stmt, rightResponses);
        }

        public void printResults(List<Translation> translationVariants, WordTables translatingFrom){
            var translatingTo = translatingFrom.getOpposite();
            if(rightResponses.isEmpty()){
                printPossibleTranslations(translationVariants, translatingTo);
            }
            else{
                System.out.print(GREEN_TEXT + "Правильно!" + RESET);
                printTypos();
                printWrongResponses();
                printUnusedTranslations(translatingTo);
            }
        }
        private void printPossibleTranslations(List<Translation> translationVariants, WordTables translatingTo){
            System.out.print(RED_TEXT + "Неправильно!" + RESET
                    + "\nМожна перекласти як: ");
            for(var trans : translationVariants)
                System.out.print(trans.getWord(translatingTo) + ", ");
        }
        private void printTypos(){
            if(!typos.isEmpty()){
                System.out.print(YELLOW_TEXT);
                if(typos.size() == 1)
                    System.out.print("\nОдрук в слові ");
                else System.out.print("\nОдруки в словах: ");
                System.out.print(RESET);//reset
                for(var pair : typos.entrySet())
                    System.out.print(pair.getValue() + ' ');
            }
        }
        private void printWrongResponses(){
            if(wrongResponses.size() == 1)
                System.out.print(RED_TEXT +"\nНеправильний переклад: " + RESET);
            else if(wrongResponses.size() > 1)
                System.out.print(RED_TEXT +"\nНеправильні переклади: " + RESET);
            for(var str : wrongResponses)
                System.out.print(str + ", ");
        }
        private void printUnusedTranslations(WordTables translatingTo){
            if(!nonUsedTranslations.isEmpty()) {
                System.out.print(BLUE_TEXT + "\nТакож можна перекласти як: " + RESET);
                for(int i = 0; i < nonUsedTranslations.size(); i++) {
                    var tmpTrans = nonUsedTranslations.get(i);
                    System.out.print(tmpTrans.getWord(translatingTo) +  ", " );
                }
            }
        }
    }
}