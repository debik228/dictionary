package Dictionary.Auxiliary;

import java.util.List;
import java.util.Random;

public class RandomDistributor {
    private double ratio;
    private Random rand;

    /**
     * @param ratio Probability that an element of an inputList will be included in outputList1
     * @param rand Pseudorandom number generator which be used for distribution elements of an inputList between two output lists
     */
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
            if (getRatio() < rand.nextDouble())     outputList1.add(curr);
            else                                    outputList2.add(curr);
        }
    }
}
