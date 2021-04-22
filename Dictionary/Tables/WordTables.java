package Dictionary.Tables;

import Dictionary.Entities.EngWord;
import Dictionary.Entities.UkrWord;
import Dictionary.Entities.Word;

public enum WordTables {
    ukr_words {
        public WordTables getOpposite() {
            return eng_words;
        }
        public Class<? extends Word> getAppropriateClass() {
            return UkrWord.class;
        }
        public RegexTables getAppropriateRegexTable() {
            return RegexTables.ukr_regex;
        }
        public Tables getTablesConstant() {
            return Tables.ukr_words;
        }
    },
    eng_words {
        public WordTables getOpposite() {
            return ukr_words;
        }
        public Class<? extends Word> getAppropriateClass() {
            return EngWord.class;
        }
        public RegexTables getAppropriateRegexTable() {
            return RegexTables.eng_regex;
        }
        public Tables getTablesConstant() {
            return Tables.eng_words;
        }

    };

    public abstract WordTables getOpposite();
    public abstract Class<? extends Word> getAppropriateClass();
    public abstract RegexTables getAppropriateRegexTable();
    public abstract Tables getTablesConstant();
}
