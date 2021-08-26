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
        if(hasMoreThanOneWord(checkingWord.word))   //прямий кут +
            return checkingWord.regex;              //пряма кут  -
                                                    //хуйня зі сплітом додана для слів з реджексами типу коротк((отривалий)|(очасний)|(ий))

        String[] wordParts = checkingWord.regex.split("[^А-Яа-яі0-9]+"),
              nonWordParts = checkingWord.regex.split("[А-Яа-яі0-9]+");

        for(int i = 0; i < wordParts.length; i++)
            if(isAdjective(wordParts[i]))
                wordParts[i] = replaceEnding(wordParts[i]);

        String res = mergeArrays(wordParts, nonWordParts);
        return res;
    }
    private boolean hasMoreThanOneWord(String str){
        return str.matches("[А-Яа-яі0-9]+ [А-Яа-яі0-9]+");
    }

    private boolean isAdjective(String str){
        return str.matches(".*(ий|а|е|і)$") || str.matches(".*(ій|я|є|і)$");
    }

    private String replaceEnding(String str){
        var res = "";
        if(hasFirstTypeEnding(str))
            res = replaceEndingType1(str);
        else
            res = replaceEndingType2(str);
        return res;
    }
    private boolean hasFirstTypeEnding(String str){
        return str.matches(".*(ий|а|е|і)$");
    }
    private String replaceEndingType1(String str){
        var regex1 = "(ий|а|е|і)";
        if(str.endsWith("ий"))str = str.substring(0, str.lastIndexOf('и'));
        if(str.endsWith("а")) str = str.substring(0, str.lastIndexOf('а'));
        if(str.endsWith("е")) str = str.substring(0, str.lastIndexOf('е'));
        if(str.endsWith("і")) str = str.substring(0, str.lastIndexOf('і'));
        str = str + regex1;
        return str;
    }
    private String replaceEndingType2(String str){
        var regex1 = "(ій|я|є|і)";
        if(str.endsWith("ій"))str = str.substring(0, str.lastIndexOf('і'));
        if(str.endsWith("я")) str = str.substring(0, str.lastIndexOf('я'));
        if(str.endsWith("є")) str = str.substring(0, str.lastIndexOf('є'));
        if(str.endsWith("і")) str = str.substring(0, str.lastIndexOf('і'));
        str = str + regex1;
        return str;
    }

    private String mergeArrays(String[] first, String[] second){
        String res = "";
        var arr1 = first[0].isEmpty()?first:second;
        var arr2 = first[0].isEmpty()?second:first;
        for(int i = 0; true; i++) {
            if(i < arr1.length) res = res + arr1[i];
            if(i < arr2.length) res = res + arr2[i];
            if(i >= arr1.length && i >= arr2.length)break;
        }
        return res;
    }
}