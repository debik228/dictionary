package Dictionary.Tables;

public enum RegexTables {
    ukr_regex{
        public RegexTables getOpposite() {
            return eng_regex;
        }
    },
    eng_regex{
        public RegexTables getOpposite() {
            return ukr_regex;
        }
    };

    public abstract RegexTables getOpposite();
}
