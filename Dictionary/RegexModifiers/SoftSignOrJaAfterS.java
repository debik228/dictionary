package Dictionary.RegexModifiers;

import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;
import Dictionary.Tables.WordTables;

/**
 * Тотожність букв "ь" і "я" після букви "с"
 * Наприклад "розвалитись" == "розвалитися"
 */
public class SoftSignOrJaAfterS implements RegexModifier {
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
        res = res.replaceAll("с[ья]", "с[ья]");
        return res;
    }
}
