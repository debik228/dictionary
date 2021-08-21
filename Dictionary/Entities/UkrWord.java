package Dictionary.Entities;

import java.util.Calendar;

public class UkrWord extends Word{
    public UkrWord(String word, int score, PoS partOfSpeech, String regex, Calendar last_upd){
        super(word, score, partOfSpeech, regex, last_upd);
    }
}
