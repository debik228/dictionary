package Dictionary;

import java.io.*;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigFile{
    public final String pathname;
    public final HashMap<String, String> params = new HashMap<>();

    public ConfigFile(String pathname)throws IOException{
        this.pathname = pathname;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(pathname));
            while(in.ready()){
                var str = in.readLine();
                if(str.matches(".* *= *.*")) {
                    var words = str.split(" *= *");
                    params.put(words[0], words[1]);
                }
            }
            in.close();
        }catch (FileNotFoundException e){
            readDBConnectParams();
            params.put("last_upd", Common.getTodayDate());
            this.saveFile();
        }
    }

    public void readDBConnectParams(){
        var in = new Scanner(System.in);
        System.out.println("Please, enter your postgres username");
        params.put("username",in.nextLine());
        System.out.println("Please, enter your postgres password. It is necessary for db connection");
        params.put("password",in.nextLine());
    }

    /**
     * @throws IOException - when executing with windows cmd line, ad saving txt file. System.out.println(userTxt.exists()) System.out.println(userTxt.canRead()) System.out.println(userTxt.canWrite()) returns true.
     */
    public void saveFile(){
        try {
            BufferedWriter out = null;
            var cfgFile = new File(pathname);
            cfgFile.createNewFile();
            out = new BufferedWriter(new FileWriter(pathname));
            for (var pair : params.entrySet())
                out.write(pair.getKey() + " = " + pair.getValue() + "\n");
            out.close();
        }catch (IOException e){throw new RuntimeException(e);}
    }

    public static String getParam(String pathname, String param) throws IOException{
        var in = new BufferedReader(new FileReader(pathname));
        String res = null;
        while(in.ready()){
            var str = in.readLine();
            if(str.matches(".* *= *.*")) {
                var words = str.split(" *= *");
                if(words[0].equals(param))
                    return words[1];
            }
        }
        throw new InvalidParameterException("Parameter " + param + " haven't found in " + pathname);
    }
    public static void setParam(String pathname, String paramName, String newValue) throws IOException{
        var cfile = new ConfigFile(pathname);
        cfile.params.put(paramName, newValue);
        cfile.saveFile();
    }
}
