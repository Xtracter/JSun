package com.crazedout.jsun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSFactory {

    private static JSFactory instance;

    private JSFactory(){
    }

    private static JSFactory getInstance(){
        if(instance==null){
            instance = new JSFactory();
        }
        return instance;
    }

    private String loadTemplate(String template){
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("/" + template);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return sb.toString();
    }

    public static String toHTMTable(JSunResult res) {
        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n");
        sb.append("var json = JSON.parse('" + res + "');\n");
        sb.append(getInstance().loadTemplate("table.js"));
        sb.append("\n</script>");
        return sb.toString();
    }

}
