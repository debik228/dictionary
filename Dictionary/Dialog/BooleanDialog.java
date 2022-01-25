package Dictionary.Dialog;

import Dictionary.Dialog.Dialog;

public class BooleanDialog extends Dialog {
    private boolean result;

    public BooleanDialog(String question) {
        super(question);
    }

    @Override
    protected void askQuestion() {
        var reply = "";
        do{
            System.out.println(question + " y/n ?");
            reply = in.nextLine();
            if(reply.matches("y(es)?") || reply.equals("+"))
                finished = result = true;
            else if(reply.matches("no?") || reply.equals("-"))
                finished = true;
        }while(!finished);
    }

    public boolean getResult(){
        assert finished;
        return result;
    }
}
