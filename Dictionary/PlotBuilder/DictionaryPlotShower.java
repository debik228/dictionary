package Dictionary.PlotBuilder;

import java.awt.*;
import java.awt.event.*;

public class DictionaryPlotShower extends Frame {
    Image img;

    public DictionaryPlotShower(){
        var source = new DictionaryPlotImageProducer().img;
        img = createImage(source);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public void paint(Graphics g){
        g.drawImage(img, getInsets().left, getInsets().top, null);
    }

    public static void main(String[] args) {
        DictionaryPlotShower win = new DictionaryPlotShower();

        win.setSize(400, 400);
        win.setVisible(true);
    }
}
