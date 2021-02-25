package Dictionary;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.HashMap;

public class ConfigFile{
    public final String pathname;
    public final HashMap<String, String> params = new HashMap<>();

    public ConfigFile(String pathname)throws IOException{
        this.pathname = pathname;
        var in = new BufferedReader(new FileReader(pathname));
        while(in.ready()){
            var str = in.readLine();
            if(str.matches(".* *= *.*")) {
                var words = str.split(" *= *");
                params.put(words[0], words[1]);
            }
        }
        in.close();
    }

    public void saveFile()throws IOException{
        var out = new BufferedWriter(new FileWriter(pathname));
        for(var pair : params.entrySet())
            out.write(pair.getKey() + " = " + pair.getValue() + "\n");
        out.close();
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

    //TODO: rewrite method as getParams
    public static void setParam(String pathname, String paramName, String newValue) throws IOException{
        var cfile = new ConfigFile(pathname);
        cfile.params.put(paramName, newValue);
        cfile.saveFile();
    }
}
