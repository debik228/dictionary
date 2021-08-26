package Dictionary;

import Dictionary.RegexModifiers.*;

import java.util.Arrays;

public enum Difficulty {
    Easy{
        public int getStandardAward(){
            return 1;
        }
        public int getMaxMultiplier(){
            return 0;
        }
        public int getMinMultiplier(){
            return 1;
        }
        public RegexModifier[] getModifiersChain() {
            return Medium.getModifiersChain();
        }
    },
    Medium{
        public int getStandardAward(){
            return 2;
        }
        public int getMaxMultiplier(){
            return 2;
        }
        public int getMinMultiplier(){
            return 1;
        }
        public RegexModifier[] getModifiersChain(){
            var hardModifiers = Hard.getModifiersChain();
            var res = Arrays.copyOf(hardModifiers, hardModifiers.length + 1);
            res[res.length - 1] = new WordCharacterDuplication();
            return res;
        }
    },
    Hard{
        public int getStandardAward(){
            return 5;
        }
        public int getMaxMultiplier(){
            return 5;
        }
        public int getMinMultiplier(){
            return 1;
        }
        public RegexModifier[] getModifiersChain(){
            return new RegexModifier[]{new EmptyModifier(),
                                       new SoftSignOrJaAfterS(),
                                       new UOrV(),
                                       new UnnecessaryTo(),
                                       new AnyEndingVariantsUkrAdj(),
                                       new IgnoreSomeNonWordCharacters(),
                                       new AbbreviationSMTH()};
        }
    };

    public abstract int getStandardAward();
    public abstract int getMinMultiplier();
    public abstract int getMaxMultiplier();
    public abstract RegexModifier[] getModifiersChain();
    }
