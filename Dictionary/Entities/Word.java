package Dictionary.Entities;

abstract public class Word {
    public final String word;
    public int score;
    public final PoS partOfSpeech;
    public final String regex;

    Word(String word, int score, PoS partOfSpeech, String regex){
        this.word = word;
        this.score = score;
        this.partOfSpeech = partOfSpeech;
        this.regex = regex;
    }

    public String toString() {
        return word;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return this.word.equals(word.word);
    }

    public enum PoS {
        Noun,           //іменник
        Verb,           //дієсл
        Adjective,      //прикм
        Adverb,         //присл(як? де? звідки? наскільки? якою мірою?)
        Pronoun,        //займ
        Preposition,    //прийменник
        Conjunction,    //сполучник
        Interjection,   //вигук
        Article,        //артикль
        PhrasalVerb,    //по типу break down і т.д.
        Idiom,          //очевидно, лол
        Unknown;        //хз

        public static PoS getConstant(String PoS){
            try { return Word.PoS.valueOf(PoS); }
            catch (IllegalArgumentException e){ return Unknown; }
        }
    }
}
