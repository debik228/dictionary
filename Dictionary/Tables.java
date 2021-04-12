package Dictionary;

public enum Tables {
    ukr_words,
    eng_words,
    translation,
    ukr_regex,
    eng_regex,
    activity_hitory;

    public static Tables getOppositeTo(Tables t){
        switch (t){
            case ukr_words:return eng_words;
            case eng_words:return ukr_words;
            case ukr_regex:return eng_regex;
            case eng_regex:return ukr_regex;
            default:throw new IllegalArgumentException("Table " + t + "hasn't opposite table.");
        }
    }
}
