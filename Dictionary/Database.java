package Dictionary;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private final String dbName;
    private final String username;
    private final String password;
    private final String cfgFilePath;

    public Database(String dbName, ConfigFile cfgFile){
        this.dbName = dbName;
        this.cfgFilePath = cfgFile.pathname;
        this.username = cfgFile.params.get("username");
        this.password = cfgFile.params.get("password");
    }

    public void initDB() throws SQLException{
        var conn = DriverManager.getConnection("jdbc:postgresql:postgres", username, password);
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
    }

    public Connection getConn(){
        Connection conn = null;
        try{
            Class.forName("org.postgresql.Driver").getConstructor().newInstance();
            conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, username, password);
        }catch (org.postgresql.util.PSQLException e){
            //TODO винести цю хуйню в окремий метод
            System.err.println("Wrong username or password. Please input correct username and password");
            var cfg = new File(cfgFilePath);
            cfg.delete();
        }
        catch (SQLException e){
            try {
                initDB();
                conn = DriverManager.getConnection("jdbc:postgresql:" + dbName, username, password);
            }catch (SQLException sqlException){throw new RuntimeException(sqlException);}
        }catch (ReflectiveOperationException e){throw new RuntimeException(e);}
        return conn;
    }
}
