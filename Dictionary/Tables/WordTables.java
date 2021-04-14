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
    },
    eng_words {
        public WordTables getOpposite() {
            return ukr_words;
        }
        public Class<? extends Word> getAppropriateClass() {
            return EngWord.class;
        }

    };

    public abstract WordTables getOpposite();
    public abstract Class<? extends Word> getAppropriateClass();
}
