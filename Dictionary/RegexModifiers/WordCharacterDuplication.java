package Dictionary.RegexModifiers;

import Dictionary.Entities.Word;

import static java.lang.Character.isLetter;

public class WordCharacterDuplication implements RegexModifier {
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
        var res = PreviouslyModifiedRegex;
        var sb = new StringBuilder();
        Character prevChar = null;
        var charArray = res.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char curr = charArray[i];
            if(prevChar != null && prevChar == curr && isLetter(curr))continue;//for case when duplication already exists
            sb.append(curr);
            prevChar = curr;
            char appending = '+';
            try{
                if(charArray[i+1] == '?' ||
                        charArray[i+1] == '+' ||
                        charArray[i+1] == '*') {
                    appending = '*';
                }
            }catch (IndexOutOfBoundsException e){}
            if(isLetter(curr))
                sb.append(appending);
        }
        res = sb.toString();
        return res;
    }
}