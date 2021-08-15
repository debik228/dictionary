package Dictionary.RegexModifiers;

import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

/**
 * boundary characters, apostrophe, comma and dot. Another non-word characters don't include because it will spoil regular expressions
 */
public class IgnoreSomeNonWordCharacters implements RegexModifier {
    @Override
    public Class<? extends Word> getLanguageSpeciality() {
        return Word.class;
    }

    @Override
    public Word.PoS getPoSSpeciality() {
        return Word.PoS.Unknown;
    }

    @Override
    public String modify(String PreviouslyModifiedRegex, final Word checkingWord) {
        var sb = new StringBuilder();
        var res = PreviouslyModifiedRegex;
        var regex = "[\\s.,'\\\\]";
        var words = res.split(regex + "+");
        for(var word : words){
            sb.append(word);
            sb.append(regex + "*");
        }
        res = sb.toString();
        return res;
    }
}
