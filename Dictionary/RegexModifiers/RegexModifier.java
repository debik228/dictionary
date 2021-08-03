package Dictionary.RegexModifiers;

import Dictionary.Entities.Word;

public interface RegexModifier {
    Class<? extends Word> getLanguageSpeciality();
    Word.PoS getPoSSpeciality();
    //boolean fitsFor(Word modifyingWord);

    String modify(String PreviouslyModifiedRegex, Word checkingWord);
}
