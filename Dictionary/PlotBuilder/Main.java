package Dictionary.PlotBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Calendar;

public class Main extends Frame{
    private final static String[] mountsNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private static String Kostyl;

    public static String render()throws Exception{
        var kusokGamna = new Main();
        kusokGamna.pizda("C:\\Users\\Yevgen\\Desktop\\pogromyvannja\\JAVA\\Dictionary\\stat");
        //System.exit(0);
        return Kostyl;
    }

    private static String getCurrentTimestamp(){
        var cal = Calendar.getInstance();
        return String.format("%02d%s%d %02d%02d%02d"
                ,cal.get(Calendar.DAY_OF_MONTH)
                ,mountsNames[cal.get(Calendar.MONTH)]
                ,cal.get(Calendar.YEAR)
                ,cal.get(Calendar.HOUR_OF_DAY)
                ,cal.get(Calendar.MINUTE)
                ,cal.get(Calendar.SECOND));
    }

    public void pizda(String path) throws Exception{
        String currentTimestamp = getCurrentTimestamp();
        Kostyl = "plot_" + currentTimestamp + ".png";
        var imgFile = new File(path + "/" + Kostyl);
        imgFile.createNewFile();
        var imgSors = new DictionaryPlotImageProducer().img;

        var image = createImage(imgSors);

        // construct the buffered image
        BufferedImage bImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

        //obtain it's graphics
        Graphics2D bImageGraphics = bImage.createGraphics();

        //draw the Image (image) into the BufferedImage (bImage)
        bImageGraphics.drawImage(image, null, null);

        // cast it to rendered image
        RenderedImage rImage      = (RenderedImage)bImage;

        var success = ImageIO.write(rImage, "png", imgFile);
    }
}
