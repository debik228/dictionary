package Dictionary.RegexModifiers;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.Word;

public class AbbreviationSMTH implements RegexModifier{
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return EngWord.class;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return Word.PoS.Unknown;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, Word checkingWord) {
        var res = PreviouslyModifiedRegex;
        res = res.replaceAll("something", "((something)|(smth))");
        res = res.replaceAll("somebody", "((somebody)|(smb)|(sb))");
        return res;
    }
}
