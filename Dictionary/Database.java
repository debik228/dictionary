package Dictionary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String dbName;
    private final ConfigFile cfgFile;
    private Connection conn;

    public Database(String dbName, ConfigFile cfgFile){
        this.dbName = dbName;
        this.cfgFile = cfgFile;
    }

    public void initDB(){
        var creatingDB = "CREATE DATABASE "+ dbName +" WITH OWNER = postgres ENCODING = 'UTF8' CONNECTION LIMIT = -1;\n";
        var createPoS =
                "CREATE TYPE public.pos AS ENUM (\n" +
                "    'Noun',\n" +
                "    'Verb',\n" +
                "    'Adjective',\n" +
                "    'Adverb',\n" +
                "    'Pronoun',\n" +
                "    'Preposition',\n" +
                "    'Conjunction',\n" +
                "    'Interjection',\n" +
                "    'Article',\n" +
                "    'PhrasalVerb',\n" +
                "    'Idiom',\n" +
                "    'Unknown'\n" +
                ");";
        var createAH =
                "CREATE TABLE public.activity_history (\n" +
                "    day date DEFAULT CURRENT_DATE NOT NULL,\n" +
                "    score integer DEFAULT 0 NOT NULL\n" +
                ");";
        var createEngWords =
                "CREATE SEQUENCE public.eng_word_id_seq\n" +
                "    START WITH 0\n" +
                "    INCREMENT BY 1\n" +
                "    MINVALUE 0\n" +
                "    NO MAXVALUE\n" +
                "    CACHE 1;" +
                "CREATE TABLE public.eng_words (\n" +
                "    id bigint DEFAULT nextval('public.eng_word_id_seq'::regclass) NOT NULL,\n" +
                "    word character varying(80) NOT NULL,\n" +
                "    score integer DEFAULT 0,\n" +
                "    last_upd date DEFAULT CURRENT_DATE,\n" +
                "    pos public.pos DEFAULT 'Unknown'::public.pos\n" +
                ");\n";
        var createUkrWords =
                "CREATE SEQUENCE public.ukr_word_id_seq\n" +
                "    START WITH 0\n" +
                "    INCREMENT BY 1\n" +
                "    MINVALUE 0\n" +
                "    NO MAXVALUE\n" +
                "    CACHE 1;" +
                "CREATE TABLE public.ukr_words (\n" +
                "    id bigint DEFAULT nextval('public.ukr_word_id_seq'::regclass) NOT NULL,\n" +
                "    word character varying(80) NOT NULL,\n" +
                "    score integer DEFAULT 0,\n" +
                "    last_upd date DEFAULT CURRENT_DATE,\n" +
                "    pos public.pos DEFAULT 'Unknown'::public.pos\n" +
                ");";
        var createTranslations =
                "CREATE TABLE public.translation (\n" +
                "    ukr_id bigint NOT NULL,\n" +
                "    eng_id bigint NOT NULL,\n" +
                "    score integer DEFAULT 0,\n" +
                "    last_training date DEFAULT (CURRENT_DATE - 1)\n" +
                ");";
        var createDictionary =
                "CREATE VIEW public.dictionary AS\n" +
                " SELECT ukr_words.word AS ukr,\n" +
                "    ukr_words.id AS ukr_id,\n" +
                "    translation.score,\n" +
                "    eng_words.id AS eng_id,\n" +
                "    eng_words.word AS translate,\n" +
                "    translation.last_training\n" +
                "   FROM ((public.ukr_words\n" +
                "     LEFT JOIN public.translation ON ((ukr_words.id = translation.ukr_id)))\n" +
                "     LEFT JOIN public.eng_words ON ((translation.eng_id = eng_words.id)));";
        var createEngRegex =
                "CREATE SEQUENCE public.eng_regex_id_seq\n" +
                "    START WITH 0\n" +
                "    INCREMENT BY 1\n" +
                "    MINVALUE 0\n" +
                "    NO MAXVALUE\n" +
                "    CACHE 1;" +
                "CREATE TABLE public.eng_regex (\n" +
                "    id integer DEFAULT nextval('public.eng_regex_id_seq'::regclass) NOT NULL,\n" +
                "    word_id integer NOT NULL,\n" +
                "    regex character varying(240) NOT NULL\n" +
                ");";
        var createUkrRegex =
                "CREATE SEQUENCE public.ukr_regex_id_seq\n" +
                "    START WITH 0\n" +
                "    INCREMENT BY 1\n" +
                "    MINVALUE 0\n" +
                "    NO MAXVALUE\n" +
                "    CACHE 1;" +
                "CREATE TABLE public.ukr_regex (\n" +
                "    id integer DEFAULT nextval('public.ukr_regex_id_seq'::regclass) NOT NULL,\n" +
                "    word_id integer NOT NULL,\n" +
                "    regex character varying(240) NOT NULL\n" +
                ");";

        try{
            var username = cfgFile.params.get("username");
            var password = cfgFile.params.get("password");
            var conn = DriverManager.getConnection("jdbc:postgresql:postgres", username, password);
            var stat = conn.createStatement();
            stat.executeUpdate(creatingDB);
            stat.close();
            conn.close();

            conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, username, password);
            stat = conn.createStatement();

            stat.executeUpdate(createPoS);
            stat.executeUpdate(createAH);
            stat.executeUpdate(createEngWords);
            stat.executeUpdate(createUkrWords);
            stat.executeUpdate(createTranslations);
            stat.executeUpdate(createDictionary);
            stat.executeUpdate(createEngRegex);
            stat.executeUpdate(createUkrRegex);
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public Statement getStatement(){
        if(this.conn == null)
            initConn();
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void initConn(){
        try{
            Class.forName("org.postgresql.Driver").getConstructor().newInstance();
            var username = cfgFile.params.get("username");
            var password = cfgFile.params.get("password");
            this.conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, username, password);
        }catch (org.postgresql.util.PSQLException e){
            if(e.getMessage() == "ResultSet not positioned properly, perhaps you need to call next."){
                System.err.println("Wrong username or password. Please input correct username and password");
                cfgFile.readDBConnectParams();
                cfgFile.saveFile();
                initConn();
            }
            else throw new RuntimeException(e);
        }catch (SQLException e){
                initDB();
                initConn();
        }catch (ReflectiveOperationException e){throw new RuntimeException(e);}
    }

    public void close(){
        try {
            this.conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
