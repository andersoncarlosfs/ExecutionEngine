package com.andersoncarlosfs.execution.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import com.andersoncarlosfs.execution.constants.Formating;
import com.andersoncarlosfs.execution.constants.Settings;
import java.util.Map;

public class WebService {

    public final class ExponentialBackOff {

        private int min;
        private int max;
        private int delay;

        public ExponentialBackOff() {
            min = 1;
            max = 30;
            delay = 0;
        }

        private int fibonacci(int n) {
            if (n <= 1) {
                return n;
            } else {
                return fibonacci(n - 1) + fibonacci(n - 2);
            }
        }

        public int delay() {
            delay = fibonacci(delay++);
            if (delay > max) {
                delay = min;
            }
            return (int) (Math.random() * (max - delay)) + delay;
        }

        public void reset() {
            delay = 0;
        }

    }

    private final ExponentialBackOff backOff = new ExponentialBackOff();

    String name;

    /**
     * The lists of fragments and variables that form the URL, on the position
     * of variables we have the value null
     */
    List<String> urlFragments;

    /**
     * The list of head variables
     */
    public List<String> headVariables;

    /**
     * The order of the variables matters! The first variables should be the
     * input variables and their order should be exactly the one required by the
     * construction of the URL
     */
    public Map<String, Integer> headVariableToPosition;

    /**
     *
     */
    public int numberOfInputs;

    /**
     * The prefixes
     */
    HashMap<String, String> prefixes;

    /**
     * The body
     */
    List<Triple> body;

    /**
     * The body currently not handled!
     */
    public WebService(String name, List<String> urlFragments, HashMap<String, String> prefixes, ArrayList<String> headVariables, HashMap<String, Integer> headVariableToPosition, int numberInputs) {
        this.name = name;
        this.urlFragments = urlFragments;
        this.headVariables = headVariables;
        this.headVariableToPosition = headVariableToPosition;
        this.numberOfInputs = numberInputs;
        this.prefixes = prefixes;
    }

    /**
     * If we do not want to read a description from a file
     */
    public WebService(String name, List<String> params) {
        this.name = name;
        this.urlFragments = params;
    }

    /**
     * @param inputs
     * @return the file where the call result is stored
     */
    public String getCallResult(String... inputs) {
        String[] args = new String[numberOfInputs];

        // Computing the arguments
        for (int i = 0; i < numberOfInputs; i++) {
            args[i] = inputs[i];
        }

        String fileWithCallResult = Settings.getDirForCallResults(this.name) + Formating.getFileNameForInputs(args);

        /**
         * Looking if the result is cached
         */
        File f = new File(fileWithCallResult);
        if (f.exists()) {
            return fileWithCallResult;
        }

        /**
         * Calling the web service
         */
        return downloadCallResults(getURLForCallWithInputs(inputs), fileWithCallResult);
    }
    
    /**
     * @param inputs
     * @return the file where the call result is stored
     */
    public String getCallResult(List<String> inputs) {
        return getCallResult(inputs.toArray(new String[inputs.size()]));
    }

    /**
     * @param fileWithCallResult
     * @return the file where the transformed result is stored
     * @throws Exception
     */
    public String getTransformationResult(String fileWithCallResult) throws Exception {
        Source callResult = new StreamSource(new File(fileWithCallResult));

        Source xsl = new StreamSource(new File(Settings.definitions + this.name + ".xsl"));

        String fileName = fileWithCallResult.substring(fileWithCallResult.lastIndexOf("/") + 1);

        String fileWithTransformationResult = Settings.getDirForTransformationResults(this.name) + fileName;

        System.out.println("File with the transformed result: " + fileWithTransformationResult);

        Result trasformResult = new StreamResult(new File(fileWithTransformationResult));

        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
        transformer.transform(callResult, trasformResult);

        return fileWithTransformationResult;
    }

    /**
     * @param inputs
     * @return the URL of the call for the given inputs
     */
    public String getURLForCallWithInputs(String... inputs) {
        int i = 0;
        StringBuffer call = new StringBuffer();
        for (String p : urlFragments) {
            if (p == null) {
                if (i >= inputs.length) {
                    return null; //something wrong; insufficient number of input values
                }
                call.append(Formating.transformStringForURL(inputs[i]));
            } else {
                call.append(p);
            }
        }
        return call.toString();
    }

    /**
     *
     * @param URL
     * @param fileForTheResults
     * @return downloads the file and stores it in the file
     */
    public String downloadCallResults(String URL, String fileForTheResults) {
        String newLine = System.getProperty("line.separator");

        BufferedReader reader = null;
        Writer writer = null;

        try {

            URL url = new URL(URL);
            URLConnection conn = url.openConnection();

            /**
             * Faking a request in order to avoid error 403
             */
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
            //conn.setRequestProperty("Accept-Charset", "UTF-8");

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            /**
             * Removing empty lines
             */
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    break;
                }
            }

            /**
             *
             */
            boolean isJSONData = false;

            if (line != null) {
                if (line.startsWith("{")) {
                    System.out.println("JSON detected");

                    isJSONData = true;

                    /**
                     * Creating a string writer if JSON detected
                     */
                    writer = new StringWriter();
                } else {
                    writer = new FileWriter(fileForTheResults);
                }
                writer.write(line + newLine);
            }

            System.out.println(line);

            /**
             * Writing the rest of the input file
             */
            while ((line = reader.readLine()) != null) {
                writer.write(line + newLine);

                System.out.println(line);
            }
            writer.flush();

            if (isJSONData) {
                /**
                 * Transforming JSON to XML
                 */
                JSONToXML.transformToXML(((StringWriter) writer).toString(), fileForTheResults);
            }
        } catch (IOException exception) {
            System.out.println("Error in the download " + URL);
            try {

                /**
                 *
                 */
                if (backOff.delay == backOff.min) {
                    return null;
                }

                int delay = backOff.delay() * 1000;

                System.out.println("Waiting " + delay + " milliseconds to retry");

                Thread.sleep(delay);

                return downloadCallResults(URL, fileForTheResults);

            } catch (InterruptedException e) {
                return null;
            } finally {
                backOff.reset();
            }
        } catch (Exception exception) {

            System.out.println("Error transformation " + URL);

            return null;

        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {

                System.out.println("Error to close resources");

                return null;

            } finally {
                backOff.reset();
            }
        }
        return fileForTheResults;
    }

}
