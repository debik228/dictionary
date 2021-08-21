package Dictionary.Entities;

import java.util.Calendar;

public class EngWord extends Word {
    public EngWord(String word, int score, PoS partOfSpeech, String regex, Calendar last_upd){
        super(word, score, partOfSpeech, regex, last_upd);
    }
}
