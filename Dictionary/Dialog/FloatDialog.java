package Dictionary.Dialog;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class FloatDialog extends Dialog {
    private float result;

    public FloatDialog(String question) {
        super(question);
    }

    @Override
    protected void askQuestion() {
        var reply = "";
        //Locale.setDefault(Locale.US);
        do{
            System.out.println(question);
            reply = in.nextLine();
            reply = reply.replaceAll(",", ".");
            if(reply.matches("\\d+\\.?\\d*")){
                result = Float.valueOf(reply);
                finished = true;
            }
        }while(!finished);
    }

    public float getResult() {
        assert finished;
        return result;
    }
}
