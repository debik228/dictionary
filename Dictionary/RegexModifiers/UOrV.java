package Dictionary.RegexModifiers;

import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

/**
 * Взаємозамінність слів "у" і "в".
 * Наприклад "у природніх умовах" == "в природніх умовах"
 * Але "вчений" != "учений"
 */
public class UOrV implements RegexModifier {
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return UkrWord.class;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return Word.PoS.Unknown;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, final Word checkingWord) {
        var res = PreviouslyModifiedRegex;
        res = res.replaceAll("[ув] ", "[ув] ");
        return res;
    }
}