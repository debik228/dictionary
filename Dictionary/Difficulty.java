package Dictionary;

public enum Difficulty {
    Easy{
        public int getStandardAward(){
            return 1;
        }
    },
    Medium{
        public int getStandardAward(){
            return 2;
        }
    },
    Hard{
        public int getStandardAward(){
            return 5;
        }
    };

    public abstract int getStandardAward();
    }
