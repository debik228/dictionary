package Dictionary.Auxiliary;

import java.util.List;
import java.util.Random;

public class RandomDistributor {
    private double ratio;
    private Random rand;

    public RandomDistributor(double ratio, Random rand) {
        this.rand = rand;
        this.ratio = ratio;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public <T> void distribute(List<T> inputList, List<T> outputList1, List<T> outputList2) {
        int initTranslationsSize = inputList.size();
        for (int i = 0; i < initTranslationsSize; i++) {
            var curr = inputList.get(rand.nextInt(inputList.size()));
            inputList.remove(curr);
            if (rand.nextDouble() > getRatio())     outputList1.add(curr);
            else                                    outputList2.add(curr);
        }
    }
}
