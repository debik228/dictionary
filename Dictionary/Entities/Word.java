package Dictionary.Entities;

import Dictionary.Tables;
import Dictionary.WordContainer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

abstract public class Word {
    public final String word;
    public int score;
    public ArrayList<Word> translations;

    Word(String word, int score){
        this.word = word;
        this.score = score;
    }

    public String toString() {
        var sb = new StringBuilder(word + ", " + score);
        if(translations != null && translations.size() != 0) {
            sb.append(", translations: ");
            for (var trans : translations) {
                sb.append(trans.word);
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return this.word.equals(word.word);
    }
}
