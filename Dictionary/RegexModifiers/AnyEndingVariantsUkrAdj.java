package Dictionary.RegexModifiers;

import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;
import Dictionary.Tables.WordTables;

public class AnyEndingVariantsUkrAdj implements RegexModifier {
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return UkrWord.class;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return Word.PoS.Adjective;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, final Word checkingWord) {
        var res = PreviouslyModifiedRegex;
        var str = checkingWord.word;
        var regex = "(ий|а|е|і)";
        if(str.endsWith("ий"))res = res.substring(0, res.lastIndexOf('и'));
        if(str.endsWith("а")) res = res.substring(0, res.lastIndexOf('а'));
        if(str.endsWith("е")) res = res.substring(0, res.lastIndexOf('е'));
        if(str.endsWith("і")) res = res.substring(0, res.lastIndexOf('і'));
        res = res + regex;
        return res;
    }
}