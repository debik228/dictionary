package Dictionary.Menus;

import java.sql.Statement;

public enum UpdateDictionary implements AbstractMenu {
    AddWords("Add Words"){
        @Override
        public void action(Statement stat) throws Exception {

        }
    },
    DefinePos("Define part of speech"){
        @Override
        public void action(Statement stat) throws Exception {

        }
    };
    public final String name;
    UpdateDictionary(String name){this.name = name;}
}
