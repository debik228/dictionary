package Dictionary;

import Dictionary.Entities.Translation;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static Dictionary.Common.sameDate;

public class checkingResult {
    private final LinkedList<Translation> rightResponses;
    private final LinkedList<String>      wrongResponses;
    private final LinkedList<Translation> nonUsedTranslations;
    private final HashMap<String, String> typos;

    public checkingResult(LinkedList<Translation> rightResponses, LinkedList<String> wrongResponses, LinkedList<Translation> nonUsedTranslations, HashMap<String, String> typos){
        this.rightResponses = rightResponses;
        this.wrongResponses = wrongResponses;
        this.nonUsedTranslations = nonUsedTranslations;
        this.typos = typos;
    }

    public LinkedList<Translation> getRightResponses() { return rightResponses; }
    public LinkedList<String> getWrongResponses() { return wrongResponses; }
    public LinkedList<Translation> getNonUsedTranslations() { return nonUsedTranslations; }
    public HashMap<String, String> getTypos() { return typos; }

    public void addScore(Statement stmt, int award)throws IOException, SQLException {
        for(var trans : rightResponses) {
            if(!sameDate(trans.last_training, Calendar.getInstance())){
                trans.addScore(award);
                Training.history.increaseDailyScore(award);
            }
            else {
                trans.addScore(1);
                Training.history.increaseDailyScore(1);
            }
            trans.last_training.setTime(new Date());
        }
        Translation.saveTranslations(stmt, rightResponses);
    }
}
