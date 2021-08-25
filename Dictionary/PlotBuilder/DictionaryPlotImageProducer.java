package Dictionary.PlotBuilder;

import java.awt.image.MemoryImageSource;

public class DictionaryPlotImageProducer{
    public final MemoryImageSource img;
    private final int h = DictionaryPlotDataGenerator.getMaxScore();
    private final int w = DictionaryPlotDataGenerator.getMaxCountOfTuplesWithSameScore();
    private int[] data = DictionaryPlotDataGenerator.getScoreDistribution();


    public DictionaryPlotImageProducer() {
        int pxls[] = new int[w * h];
        int avg = DictionaryPlotDataGenerator.getAvgScore();
        int bias = DictionaryPlotDataGenerator.getBias();
        int i = 0;

        int r = 255, g = 0, b = 0;
        int plotColour = (255 << 24) | (r << 16) | (g << 8) | b;
        int averageColour = (255 << 24) | (255 << 16) | (220 << 8);
        int zeroColour = 0;//black
        int whiteColour = (255 << 24) | (255 << 16) | (255 << 8) | 255;


        for (int y = 0; y < h; y++) {
            int rowColour = y==avg+bias?averageColour:(y==0+bias?zeroColour:plotColour);
            for (int x = 0; x < w; x++) {
                if (x <= data[y]) pxls[i++] = rowColour;
                else pxls[i++] = whiteColour;
            }
        }
        img = new MemoryImageSource(w, h, pxls, 0, w);
    }
}
