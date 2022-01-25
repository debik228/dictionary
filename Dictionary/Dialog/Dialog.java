package Dictionary.Dialog;

import java.util.Scanner;

public abstract class Dialog {
    protected String question;
    protected boolean finished = false;
    protected Scanner in = in = new Scanner(System.in);

    public Dialog(String question){
        this.question = question;
        askQuestion();
    }
    protected abstract void askQuestion();
}
