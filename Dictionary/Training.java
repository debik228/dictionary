package Dictionary;

import Dictionary.Entities.Translation;
import Dictionary.Entities.Word;

import java.io.*;
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

        var user = new ConfigFile("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\user.txt");
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
            inp = Translation.loadTranslations(stmt, "score < 5");

            res = trainingStatement(stmt, false, 5, inp, Hard);
            if (res.size() > 0) res = trainingStatement(stmt, false, 2, res, Medium);
            if (res.size() > 0) trainingStatement(stmt, true, 1, res, Easy);
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

    public static List<Translation> trainingStatement(Statement stmt, boolean looped, int award, List<Translation> translations, Difficulty difficulty)throws SQLException, IOException{

        //DEBUG
        System.out.println(difficulty.name());
        //END DEBUG

        var nextTour = new LinkedList<Translation>();
        Translation currTrans = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        do {
            currTrans = translations.get(0);
            System.out.println("Як перекладаєцця на англійську " + ukrWords.get(currTrans.ukr_id).word);
            var response = new HashSet<String>();
            Collections.addAll(response, in.readLine().toLowerCase().split(", *"));
            var translationVariants = Translation.loadTranslations(stmt, "ukr_id = " + currTrans.ukr_id);

            var checkingResult = checkResponse(response, translationVariants, difficulty);
            LinkedList<Translation>  rightResponses       = checkingResult.rightResponses;
            LinkedList<String>       wrongResponses       = checkingResult.wrongResponses;
            LinkedList<Translation>  nonUsedTranslations  = checkingResult.nonUsedTranslations;
            HashMap<String, String>  typos                = checkingResult.typos;


            for(var trans : rightResponses)
                if(trans.last_upd.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)         //if didn`t train
                        || trans.last_upd.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))                      //today
                    trans.addScore(award);
                else
                    trans.addScore(1);
            Translation.saveTranslations(stmt, rightResponses);
            translations.removeAll(rightResponses);

            if(rightResponses.size() == 0){
                translations.remove(0);
                nextTour.add(currTrans);
                System.out.print("Неправильно!\nМожна перекласти як: ");
                for(var trans : translationVariants)
                    System.out.print(engWords.get(trans.eng_id).word + ", ");
            }
            else{
                System.out.print("Правильно!");
                if(typos.size() > 0){
                    if(typos.size() == 1)
                        System.out.print("\nОдрук в слові ");
                    else System.out.print("\nОдруки в словах: ");
                    for(var pair : typos.entrySet())
                        System.out.print(pair.getValue() + ' ');
                }
                if(wrongResponses.size() > 0){
                    System.out.print("Неправильні переклади: ");
                    for(var str : wrongResponses)
                        System.out.print(str + ", ");
                }
                if(nonUsedTranslations.size() > 0) { //There is non used eng translation of this word
                    System.out.print("Також можна перекласти як: ");
                    for(int i = 0; i < nonUsedTranslations.size(); i++) { //Remove all the non used translation
                        var tmpTrans = nonUsedTranslations.get(i);
                        translations.removeAll(nonUsedTranslations);
                        System.out.print(engWords.get(tmpTrans.eng_id).word + ' ');
                    }
                }

            }
            Translation.saveTranslations(stmt, rightResponses);
            System.out.println();
            if(translations.size() == 0 && looped) translations = nextTour;
        }while(translations.size() != 0);
        return nextTour;
    }

    public static checkingResult checkResponse(HashSet<String> responses, List<Translation> translationsVariants, Difficulty difficulty){
        var rightResponses = new LinkedList<Translation>();
        var wrongResponses = new LinkedList<String>();
        var nonUsedTranslations = new LinkedList<>(translationsVariants);
        var typos = new HashMap<String, String>();

        for(String response : responses){
            var wrong = true;
            for(int i = 0; i < translationsVariants.size(); i++) {
                var currTrans = translationsVariants.get(i);
                String checkedEngWord = engWords.get(currTrans.eng_id).word.toLowerCase();
                String modified = modifyString(response, difficulty);
                if(checkedEngWord.matches(modified)){
                    rightResponses.add(currTrans);
                    nonUsedTranslations.remove(currTrans);
                    wrong = false;
                    if(!checkedEngWord.equals(response))//typo
                        typos.put(response, checkedEngWord);
                    break;
                }
            }
            if(wrong)wrongResponses.add(response);
        }

        return new checkingResult(rightResponses, wrongResponses, nonUsedTranslations, typos);
    }

    public static String modifyString(String str, Difficulty difficulty){
        var res = str;
        switch (difficulty) {
            case Hard:
                break;
            case Easy:
                str = str.replaceAll("[ck]", "[ck]");
            case Medium:
                var sb = new StringBuilder();
                for (var ch : str.toCharArray()) {
                    sb.append(ch);
                    sb.append('*');
                }
                res = sb.toString();
                break;

        }
        return res;
    }
}
