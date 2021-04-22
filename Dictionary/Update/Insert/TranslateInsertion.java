package Dictionary.Update.Insert;

import Dictionary.Common;
import Dictionary.Entities.Translation;
import Dictionary.Tables.Tables;

import java.sql.SQLException;
import java.sql.Statement;

public class TranslateInsertion extends Insertion{
    private final int initialScore;
    private final WordInsertion ukrInsertion;
    private final WordInsertion engInsertion;

    public TranslateInsertion(WordInsertion ukrInsertion, WordInsertion engInsertion)throws SQLException {
        this.ukrInsertion = ukrInsertion;
        this.engInsertion = engInsertion;
        this.initialScore = Math.min(Translation.getMinScore(), 0);
        executeInsertion();
    }

    protected String formVALUESStatement() throws SQLException {
        var VALUES = new StringBuilder();
        for(int ukrIndex = 0; ukrIndex < ukrInsertion.correspondingIds.length; ukrIndex++)
            for(int engIndex = 0; engIndex < engInsertion.correspondingIds.length; engIndex++){
                if(ukrInsertion.hasJustInserted[ukrIndex] || engInsertion.hasJustInserted[engIndex])
                    VALUES.append(addNewPair(ukrIndex, engIndex));
                else
                if(!Common.contains(Tables.translation,
                        String.format("ukr_id=%d AND eng_id=%d",
                                ukrInsertion.correspondingIds[ukrIndex],
                                engInsertion.correspondingIds[engIndex])))
                    VALUES.append(addNewPair(ukrIndex, engIndex));
            }
        if(VALUES.length() > 0)
            VALUES.setCharAt(VALUES.length()-1, '\n');
        return VALUES.toString();
    }
    private String addNewPair(int ukrIndex, int engIndex){
        return String.format("(%d, %d, %d),",
                ukrInsertion.correspondingIds[ukrIndex],
                engInsertion.correspondingIds[engIndex],
                initialScore);
    }

    protected String formINSERTStatement() {
        return "INSERT INTO translation (ukr_id, eng_id, score) VALUES ";
    }
}
