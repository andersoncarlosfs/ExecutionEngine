package com.andersoncarlosfs.execution;

import com.andersoncarlosfs.execution.parsers.ParseResultsForWS;
import com.andersoncarlosfs.execution.parsers.WebServiceDescription;
import com.andersoncarlosfs.execution.download.WebService;
import java.util.List;

public class Main {

    /**
     * 
     */
    private static String[] getElements(String part) {
        String[] elements = part.split(",");

        for (int i = 0; i < elements.length; i++) {
            elements[i] = elements[i].trim();
        }

        return elements;
    }

    /**
     *
     */
    private static List<String[]> getTuples(String description, String... parameters) throws Exception {

        WebService ws = WebServiceDescription.loadDescription(description);

        System.out.println(description);

        String fileWithCallResult = ws.getCallResult(parameters);

        System.out.println("The call is: " + fileWithCallResult);

        String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);

        return ParseResultsForWS.showResults(fileWithTransfResults, ws);
    }

    public static final void main(String[] args) throws Exception {
        args = new String[1];

        args[0] = "P(?title, ?year)<-getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#getAlbumByArtistId(?id, ?aid, ?albumName)";

        String expression = args[0];

        String[] parts = expression.split("<-");

        if (parts.length != 2) {
            System.out.println("Expression not well formed");

            System.exit(0);
        }

        String head = parts[0];
        String body = parts[1];

        List<String[]> listOfTupleResult = getTuples("mb_getArtistInfoByName", "Frank Sinatra");

        System.out.println("The tuple results are:");
        for (String[] tuple : listOfTupleResult) {
            System.out.print("(");
            for (int i = 0; i < tuple.length - 1; i++) {
                System.out.print(tuple[i] + ", ");
            }
            System.out.print(tuple[tuple.length - 1]);
            System.out.print(")");
            System.out.println();
        }

        System.out.println();

        for (String[] values : listOfTupleResult) {

            String artistId = values[1];

            List<String[]> listOfTupleResult2 = getTuples("mb_getAlbumByArtistId", artistId);

            System.out.println("The tuple results are:");
            for (String[] tuple : listOfTupleResult2) {
                System.out.print("(");
                for (int i = 0; i < tuple.length - 1; i++) {
                    System.out.print(tuple[i] + ", ");
                }
                System.out.print(tuple[tuple.length - 1]);
                System.out.print(")");
                System.out.println();
            }

        }

    }

}
