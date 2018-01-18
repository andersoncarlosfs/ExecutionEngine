package com.andersoncarlosfs.execution.download;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

public class JSONToXML {

    public static String readFileContent(String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (Exception exception) {
            System.out.println("Error in the content");

            throw exception;
        }
        return content.toString();
    }

    public static final void storeContentInFile(String content, String filePath) throws Exception {
        try (FileWriter fOut = new FileWriter(filePath)) {
            fOut.write(content);
        } catch (Exception exception) {
            System.out.println("Error in the storage");

            throw exception;
        }
    }

    public static final void transformToXML(String jsonData, String destinationPath) throws Exception {
        storeContentInFile(new XMLSerializer().write(JSONSerializer.toJSON(jsonData)), destinationPath);

        //System.out.println(xml);
    }

}
