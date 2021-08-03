package Dictionary.RegexModifiers;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.Word;

public class UnnecessaryTo implements RegexModifier {
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return EngWord.class;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return Word.PoS.Verb;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, final Word checkingWord) {
        var res = PreviouslyModifiedRegex;
        var str = checkingWord.word;
        if (str.startsWith("to "))
            res = res.replaceFirst("to ", "(to )?");
        else
            res = "(to )?" + res;
        return res;
    }
}
