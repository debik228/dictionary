package Dictionary;

import Dictionary.Entities.Translation;

import java.util.HashMap;
import java.util.LinkedList;

public class checkingResult {
    public final LinkedList<Translation> rightResponses;
    public final LinkedList<String>      wrongResponses;
    public final LinkedList<Translation> nonUsedTranslations;
    public final HashMap<String, String> typos;

    public checkingResult(LinkedList<Translation> rightResponses, LinkedList<String> wrongResponses, LinkedList<Translation> nonUsedTranslations, HashMap<String, String> typos){
        this.rightResponses = rightResponses;
        this.wrongResponses = wrongResponses;
        this.nonUsedTranslations = nonUsedTranslations;
        this.typos = typos;
    }
}
