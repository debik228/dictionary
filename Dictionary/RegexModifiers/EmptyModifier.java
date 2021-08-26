package Dictionary.RegexModifiers;

import Dictionary.Entities.Word;

public class EmptyModifier implements RegexModifier{
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return null;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return null;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, Word checkingWord) {
        return checkingWord.regex;
    }
}
