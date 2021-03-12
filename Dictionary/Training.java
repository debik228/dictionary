package Dictionary;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Translation;
import Dictionary.Entities.Word;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static Dictionary.Difficulty.*;
import static java.lang.Character.isLetter;

public class Training {

    static HashMap<Integer, Word> engWords;
    static HashMap<Integer, Word> ukrWords;
    static ActivityHistory history;

    public static void train(Statement stmt) throws SQLException, IOException {

        engWords = Common.loadWordTable(stmt, Tables.eng_words);
        ukrWords = Common.loadWordTable(stmt, Tables.ukr_words);
        history = new ActivityHistory(stmt);

        //var user = new ConfigFile("C:/Users/Yevgen/Desktop/pogromyvannja/JAVA/Dictionary/user.txt");
        //boolean lastTrainWasToday = Common.isToday(user.params.get("last_training"));

        //if(!lastTrainWasToday) {
        //    //updating info in user.txt
        //    var today = Calendar.getInstance();
        //    user.params.put("last_training", today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.MONTH)+1 + "-" + today.get(Calendar.YEAR));
        //    user.saveFile();
        //}

        //the training
        List<Translation> inp, res;
        inp = Translation.loadTranslations(stmt, "score <= (SELECT avg(score) FROM dictionary)");

        res = trainingStatement(stmt, 5, inp, Hard);
        if (res.size() > 0) res = trainingStatement(stmt, 2, res, Medium);
        while(res.size() > 0)
            res = trainingStatement(stmt, 1, res, Easy);
        history.saveDailyScore(stmt);
    }

    public static List<Translation> trainingStatement(Statement stmt, int award, List<Translation> translations, Difficulty difficulty)throws SQLException, IOException{
        System.out.println(difficulty.name());
        var nextTour = new LinkedList<Translation>();

        var rand = new Random();
        var ukrToEng = new LinkedList<Translation>();
        var engToUkr = new LinkedList<Translation>();
        int initTranslationsSize = translations.size();
        for(int i = 0; i < initTranslationsSize; i++){
            var curr = translations.get(rand.nextInt(translations.size()));
            translations.remove(curr);
            if(rand.nextBoolean())  ukrToEng.add(curr);
            else                    engToUkr.add(curr);
        }

        if(engToUkr.size() > 0) nextTour.addAll(
                trainingHalfStatement(stmt, award, engToUkr, difficulty, Tables.eng_words));

        if(ukrToEng.size() > 0) nextTour.addAll(
                trainingHalfStatement(stmt, award, ukrToEng, difficulty, Tables.ukr_words));
        return nextTour;
    }

    public static List<Translation> trainingHalfStatement(Statement stmt, int award, List<Translation> translations, Difficulty difficulty, Tables from)throws SQLException, IOException{
        int questionsTot = translations.size(),
            questionsNum = 1;
        var source = from == Tables.eng_words ? engWords:ukrWords;
        var scope  = from == Tables.eng_words ? ukrWords:engWords;

        Translation currTrans = null;
        var nextTour = new LinkedList<Translation>();
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        while(translations.size() != 0){
            currTrans = translations.get(0);
            int loadedWordId = from == Tables.ukr_words ? currTrans.ukr_id:currTrans.eng_id;
            System.out.println("\u001B[37m" + questionsNum + "/" + questionsTot + " Як перекладаєцця " + source.get(loadedWordId).word + "\u001B[0m");
            var response = new HashSet<String>();
            Collections.addAll(response, in.nextLine().toLowerCase().split(", *"));
            var translationVariants = from == Tables.ukr_words ? Translation.loadTranslations(stmt, "ukr_id = " + loadedWordId) : Translation.loadTranslations(stmt, "eng_id = " + loadedWordId);

            var checkingResult = checkResponse(response, translationVariants, difficulty, from);
            LinkedList<Translation>  rightResponses       = checkingResult.rightResponses;
            LinkedList<String>       wrongResponses       = checkingResult.wrongResponses;
            LinkedList<Translation>  nonUsedTranslations  = checkingResult.nonUsedTranslations;
            HashMap<String, String>  typos                = checkingResult.typos;


            for(var trans : rightResponses) {
                if (trans.last_training.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)         //if didn`t train
                        || trans.last_training.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {           //today
                    trans.addScore(award);
                    history.increaseDailyScore(award);
                }
                else {
                    trans.addScore(1);
                    history.increaseDailyScore(1);
                }
                trans.last_training.setTime(new Date());
            }
            Translation.saveTranslations(stmt, rightResponses);
            translations.removeAll(rightResponses);

            if(rightResponses.size() == 0){
                translations.remove(currTrans);
                nextTour.add(currTrans);
                System.out.print("\u001B[31m" + "Неправильно!" + "\u001B[0m"
                        + "\nМожна перекласти як: ");
                for(var trans : translationVariants)
                    if(from == Tables.ukr_words)    System.out.print(scope.get(trans.eng_id).word + ", ");
                    else                            System.out.print(scope.get(trans.ukr_id).word + ", ");
            }
            else{
                System.out.print("\u001B[32m" + "Правильно!" + "\u001B[0m");
                if(typos.size() > 0){
                    System.out.print("\u001B[33m");//yellow
                    if(typos.size() == 1)
                        System.out.print("\nОдрук в слові ");
                    else System.out.print("\nОдруки в словах: ");
                    System.out.print("\u001B[0m");//reset
                    for(var pair : typos.entrySet())
                        System.out.print(pair.getValue() + ' ');
                }
                if(wrongResponses.size() > 0){
                    System.out.print("\u001B[31m" +"\nНеправильні переклади: " + "\u001B[0m");
                    for(var str : wrongResponses)
                        System.out.print(str + ", ");
                }
                if(nonUsedTranslations.size() > 0) { //There is non used translation of this word
                    System.out.print("\u001B[34m" + "\nТакож можна перекласти як: " + "\u001B[0m");
                    for(int i = 0; i < nonUsedTranslations.size(); i++) {
                        var tmpTrans = nonUsedTranslations.get(i);
                        if(from == Tables.ukr_words)    System.out.print(scope.get(tmpTrans.eng_id).word + ", " );
                        else                            System.out.print(scope.get(tmpTrans.ukr_id).word + ", " );
                    }
                }
            }
            translations.removeAll(nonUsedTranslations);//we've show all the variants, so we shouldn't give a chance to give a right response in this tour
            Translation.saveTranslations(stmt, rightResponses);
            System.out.println();
            questionsNum++;
        }
        return nextTour;
    }

    public static checkingResult checkResponse(HashSet<String> responses, List<Translation> translationsVariants, Difficulty difficulty, Tables from){
        var rightResponses = new LinkedList<Translation>();
        var wrongResponses = new LinkedList<String>();
        var nonUsedTranslations = new LinkedList<>(translationsVariants);
        var typos = new HashMap<String, String>();

        for(String response : responses){
            var wrong = true;
            for(int i = 0; i < translationsVariants.size(); i++) {
                var currTrans = translationsVariants.get(i);
                Word checkedScope = from == Tables.ukr_words ? engWords.get(currTrans.eng_id) : ukrWords.get(currTrans.ukr_id);
                String modified = modifyString(difficulty, checkedScope);
                if(response.toLowerCase().matches(modified)){
                    rightResponses.add(currTrans);
                    nonUsedTranslations.remove(currTrans);
                    wrong = false;
                    if(difficulty != Hard && !response.toLowerCase().matches(modifyString(Hard, checkedScope)))//In fact hard difficulty, don't allow mistakes, but allows typos. So mistake list always empty in this level.
                        typos.put(response, checkedScope.word.toLowerCase());
                    break;
                }
            }
            if(wrong)wrongResponses.add(response);
        }

        return new checkingResult(rightResponses, wrongResponses, nonUsedTranslations, typos);
    }

    public static String modifyString(Difficulty difficulty, Word checkedScope){
        var str = checkedScope.regex.toLowerCase();
        var res = str;//str is an original word. Please don't modify it.
        var sb = new StringBuilder();
        String regex;

        //sameness of endings -сь , -ся e.g. розпадатись = розпадатися
        sb.setLength(0);//reset sb
        if(str.matches(".*с[ья]\\s*.*")){
            var modWords = res.split(" +");
            var origWords = str.split(" +");
            for(int i = 0; i < modWords.length; i++){
                if(origWords[i].matches(".*с[ья]"))
                    modWords[i] = modWords[i].substring(0, Math.max(modWords[i].lastIndexOf('я'), modWords[i].lastIndexOf('ь'))) + "[ья]";  //inserts [ья] instead of ending of modWords[i]
                sb.append(modWords[i]);
                sb.append(" ");
            }
            res = sb.toString();
        }

        //unnecessary 'to' before verbs
        if(checkedScope.getClass() == EngWord.class && checkedScope.partOfSpeech == Word.PoS.Verb) {
            if (str.startsWith("to "))
                res = res.replaceAll("to ", "(to )?");
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

}
