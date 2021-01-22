package Dictionary;

import Dictionary.Entities.Translation;
import Dictionary.Entities.Word;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static Dictionary.Difficulty.*;

public class Training {

    static HashMap<Integer, Word> engWords;
    static HashMap<Integer, Word> ukrWords;

    public static void train(Statement stmt) throws SQLException, IOException {

        engWords = Common.loadWordTable(stmt, Tables.eng_words);
        ukrWords = Common.loadWordTable(stmt, Tables.ukr_words);

        var user = new ConfigFile("C:/Users/Yevgen/Desktop/pogromyvannja/JAVA/Dictionary/user.txt");
        boolean lastTrainWasToday = Common.isToday(user.params.get("last_training"));

        if(!lastTrainWasToday) {
            //updating info in user.txt
            var today = Calendar.getInstance();
            user.params.put("last_training", today.get(Calendar.DAY_OF_MONTH) + "-" + today.get(Calendar.MONTH) + "-" + today.get(Calendar.YEAR));
            user.saveFile();
        }

        //the training
        List<Translation> inp = null, res = null;
        try {
            inp = Translation.loadTranslations(stmt, "score < (SELECT avg(score) FROM dictionary)");

            res = trainingStatement(stmt, 5, inp, Hard);
            if (res.size() > 0) res = trainingStatement(stmt, 2, res, Medium);
            while(res.size() > 0)
                res = trainingStatement(stmt, 1, res, Easy);
        }
        catch (Exception e){
            e.printStackTrace();
            var logFile = new File("log.txt");
            var out = new BufferedWriter(new FileWriter(logFile));
            out.write(e.toString() + "\n");
            for(var cause : e.getStackTrace())
                out.write("\tat " + cause + '\n');

            out.write("\ninp:\n");
            for (var trans : inp)
                out.write(trans.toString() +  '\n');

            out.write("\nres:\n");
            if(res != null)
                for (var trans : res)
                    out.write(trans.toString() + '\n');
            out.flush();
            out.close();
        }
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
        //if(engToUkr.size() > 0) nextTour.addAll(
        //        trainingHalfStatement(stmt, award, engToUkr, difficulty, Tables.eng_words));
        return nextTour;
    }

    public static List<Translation> trainingHalfStatement(Statement stmt, int award, List<Translation> translations, Difficulty difficulty, Tables from)throws SQLException, IOException{
        var source = from == Tables.eng_words ? engWords:ukrWords;
        var scope  = from == Tables.eng_words ? ukrWords:engWords;

        Translation currTrans = null;
        var nextTour = new LinkedList<Translation>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while(translations.size() != 0){
            currTrans = translations.get(0);
            int loadedWordId = from == Tables.ukr_words ? currTrans.ukr_id:currTrans.eng_id;
            System.out.println("\u001B[37m" + "Як перекладаєцця на англійську " + source.get(loadedWordId).word + "\u001B[0m");
            var response = new HashSet<String>();
            Collections.addAll(response, in.readLine().toLowerCase().split(", *"));
            var translationVariants = from == Tables.ukr_words ? Translation.loadTranslations(stmt, "ukr_id = " + loadedWordId) : Translation.loadTranslations(stmt, "eng_id = " + loadedWordId);

            var checkingResult = checkResponse(response, translationVariants, difficulty, from);
            LinkedList<Translation>  rightResponses       = checkingResult.rightResponses;
            LinkedList<String>       wrongResponses       = checkingResult.wrongResponses;
            LinkedList<Translation>  nonUsedTranslations  = checkingResult.nonUsedTranslations;
            HashMap<String, String>  typos                = checkingResult.typos;


            for(var trans : rightResponses)
                if(trans.last_upd.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)         //if didn`t train
                        || trans.last_upd.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))              //today
                    trans.addScore(award);
                else
                    trans.addScore(1);
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
                        if(from == Tables.ukr_words)    System.out.print(scope.get(tmpTrans.eng_id).word + ' ');
                        else                            System.out.print(scope.get(tmpTrans.ukr_id).word + ' ');
                    }
                }
            }
            translations.removeAll(nonUsedTranslations);                                                                //we've show all the variants, so we shouldn't give a chance to give a right response in this tour
            Translation.saveTranslations(stmt, rightResponses);
            System.out.println();
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
                String checkedScopeWord = from == Tables.ukr_words ? engWords.get(currTrans.eng_id).word.toLowerCase() : ukrWords.get(currTrans.ukr_id).word.toLowerCase();
                String modified = modifyString(response, difficulty);
                if(checkedScopeWord.matches(modified)){
                    rightResponses.add(currTrans);
                    nonUsedTranslations.remove(currTrans);
                    wrong = false;
                    if(!checkedScopeWord.equals(response))//typo.
                        if(difficulty != Hard)//In fact hard difficulty level modifying strings, but don't allow typo. So typo list always empty in this level.
                            typos.put(response, checkedScopeWord);
                    break;
                }
            }
            if(wrong)wrongResponses.add(response);
        }

        return new checkingResult(rightResponses, wrongResponses, nonUsedTranslations, typos);
    }

    public static String modifyString(String str, Difficulty difficulty){
        var res = str;//str is an original word. Please don't modify it.
        var sb = new StringBuilder();

        switch (difficulty) {
            case Easy:
                //res = res.replaceAll("[ck]", "[ck]");
            case Medium:
                sb.setLength(0);//reset sb

                //allow any word-character duplication
                Character prevChar = null;
                for (var ch : res.toCharArray()) {
                    if(prevChar != null && prevChar == ch)continue;
                    sb.append(ch);
                    prevChar = ch;
                    if((ch >= 'a' && ch <= 'z') ||    //a-z
                       (ch >= 'A' && ch <= 'Z') ||    //A-Z
                       (ch >= 'а' && ch <= 'я') ||    //а-я
                       (ch >= 'А' && ch <= 'Я'))      //А-Я
                        sb.append('+');
                }
                res = sb.toString();
            case Hard:
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

                //ignore some non-word-characters
                sb.setLength(0);//reset sb
                String regex = "[\\s.,']";                   //boundary characters, apostrophe, comma and dot. Another non-word characters don't include because it will spoil regular expressions
                var words = res.split(regex + "+");   //previously used [^а-яА-Я^\w]
                for(var word : words){
                    sb.append(word);
                    sb.append(regex + "*");
                }
                res = sb.toString();
        }
        return res;
    }
}
