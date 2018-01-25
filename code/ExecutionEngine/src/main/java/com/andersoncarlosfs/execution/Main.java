package com.andersoncarlosfs.execution;

import com.andersoncarlosfs.execution.parsers.ParseResultsForWS;
import com.andersoncarlosfs.execution.parsers.WebServiceDescription;
import com.andersoncarlosfs.execution.download.WebService;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {

    private static class Expression {

        private static class Element {

            private String value;

            public Element(String value) {
                this.value = value;
            }

            public Boolean isVariable() {
                return value.startsWith("?");
            }

        }

        private String function;
        private List<Element> elements;

        public Expression(String function, List<Element> elements) {
            this.function = function;
            this.elements = elements;
        }

        /**
         *
         */
        public List<String> getElementsAsListOfString() {
            List<String> list = new LinkedList<>();
            for (Element element : elements) {
                list.add(element.value);
            }
            return list;
        }

        /**
         *
         */
        private static List<Element> getElements(String body) {
            // Removing the ")" from the body of the expression
            String[] parts = body.split("\\)");
            for (int i = 1; i < parts.length; i++) {
                // Chencking if body of the expression contains unexpected parts
                if (!parts[i].trim().isEmpty()) {
                    return Collections.EMPTY_LIST;
                }
            }
            // Checking if the body of the expression is empty
            if (parts[0].trim().isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            // Spliting the elements
            parts = parts[0].split(",");
            List<Element> elements = new LinkedList<>();
            for (int i = 0; i < parts.length; i++) {
                String element = parts[i].trim();
                // Checking if the element is empty
                if (!element.isEmpty()) {
                    elements.add(new Element(element));
                }
            }
            return elements;
        }

        /**
         *
         */
        private static Expression getExpression(String expression) {
            // Spliting the function and the body of the expression
            String[] parts = expression.split("\\(");
            // Checking if the expression is composed by two (2) parts
            if (parts.length != 2) {
                return null;
            }
            String function = parts[0].trim();
            // Checking if the function of the expression is empty
            if (function.isEmpty()) {
                return null;
            }
            List<Element> elements = getElements(parts[1]);
            return elements.isEmpty() ? null : new Expression(function, elements);
        }

        /**
         *
         */
        private static List<Expression> getListOfExpression(String stringExpression) {
            // Spliting the expressions
            String[] arrayOfStringExpressions = stringExpression.split("#");
            List<Expression> listOfObjectExpressions = new LinkedList<>();
            for (int i = 0; i < arrayOfStringExpressions.length; i++) {
                stringExpression = arrayOfStringExpressions[i].trim();
                // Checking if the function of the expression is empty
                if (stringExpression.isEmpty()) {
                    return null;
                }
                Expression objectExpression = getExpression(stringExpression);
                // Chencking if body of the expression contains unexpected parts
                if (objectExpression == null) {
                    return null;
                }
                listOfObjectExpressions.add(objectExpression);
            }
            return listOfObjectExpressions;
        }

        /**
         *
         */
        private static Map.Entry<Expression, List<Expression>> getQuery(String query) {
            // Spliting the left hand side and the right hand side of the query
            String[] parts = query.split("<-");
            // Checking if the query is composed by two (2) parts
            if (parts.length != 2) {
                //return null;
                System.out.println("Query not well formed: The query does not contain a head and a body well defined");

                System.exit(0);
            }
            Expression head = getExpression(parts[0]);
            List<Expression> body = getListOfExpression(parts[1]);
            // Checking if the query contains a head
            // Checking if the query contains a body
            //return head == null || body == null ? null : new AbstractMap.SimpleEntry(head, body);
            if (head == null) {
                System.out.println("Query not well formed: The query does not contain a head");

                System.exit(0);
            }
            if (body == null) {
                System.out.println("Query not well formed: The query does not contain a body");

                System.exit(0);
            }
            return new AbstractMap.SimpleEntry(head, body);
        }

        /**
         *
         */
        private static boolean isQueryConsitent(Map.Entry<Expression, List<Expression>> query) {
            // Checking if the query is empty
            if (query == null) {
                return false;
            }
            List<String> headConstants = new LinkedList<>();
            List<String> headVariables = new LinkedList<>();
            for (Element element : query.getKey().elements) {
                if (element.isVariable()) {
                    headVariables.add(element.value);
                } else {
                    headConstants.add(element.value);
                }
            }
            List<String> bodyConstants = new LinkedList<>();
            List<String> bodyVariables = new LinkedList<>();
            for (Expression expression : query.getValue()) {

                WebService ws = WebServiceDescription.loadDescription(expression.function);

                // Checking if the function is defined
                if (ws == null) {
                    System.out.println("Query not well formed: The function \"" + expression.function + "\" is not defined");

                    System.exit(0);
                }

                if (ws.headVariables.size() != expression.elements.size()) {
                    System.out.println("Query not well formed: The function \"" + expression.function + "\" was defined with " + ws.headVariables.size() + " arguments and " + expression.elements.size() + " arguments was found");

                    System.exit(0);
                }

                for (Element element : expression.elements) {
                    if (element.isVariable()) {
                        bodyVariables.add(element.value);
                    } else {
                        bodyConstants.add(element.value);
                    }
                }

                List<Element> inputs = new LinkedList<>();
                for (int i = 0; i < ws.numberOfInputs; i++) {
                    Element element = expression.elements.get(i);
                    if (element.isVariable()) {
                        for (Expression e : query.getValue()) {
                            if (e.elements.contains(element) && inputs.add(element)) {
                                break;
                            }
                        }
                    } else {
                        inputs.add(element);
                    }
                }
                if (ws.numberOfInputs != inputs.size()) {
                    System.out.println("Query not well formed: The function \"" + expression.function + "\" was defined with " + ws.numberOfInputs + " inputs and " + inputs + " inputs was found");

                    System.exit(0);
                }

            }
            // Checking if the head is not empty
            // Checking if the body contains a least one constant
            // Checking if the body contains all variables in the head 
            //return !query.getKey().elements.isEmpty() && !bodyConstants.isEmpty() && bodyVariables.contains(headVariables);
            if (query.getKey().elements.isEmpty()) {
                System.out.println("Query not well formed: The function \"" + query.getKey().function + "\" does not have body");

                System.exit(0);
            }
            if (bodyConstants.isEmpty()) {
                System.out.println("Query not well formed: The body of the query only contains variables");

                System.exit(0);
            }
            if (!bodyVariables.containsAll(headVariables)) {
                System.out.println("Query not well formed: The head of the query contains only variables that does not appear on the body of the query");

                System.exit(0);
            }
            return true;
        }

    }

    private static class Relation {

        private static class Row {

            private List<String> values;

            public Row() {
                this.values = new LinkedList<>();
            }

            private Row(Row row) {
                this.values = new LinkedList<>(row.values);
            }

        }

        private List<String> header;
        private List<Row> rows;

        private Relation() {
            this.header = new LinkedList<>();
            this.rows = new LinkedList<>();
        }

        private Relation(List<String> header, List<Row> rows) {
            this.header = header;
            this.rows = rows;
        }

        /**
         *
         */
        private int getIndexOfHeaderOrAlias(String variable) {
            // Checking if the element is a variable    
            /*
            if (!element.isVariable()) {
                return -1;
            }            
             */
            // Checking if the header contain the variable            
            int index = 0;
            for (String value : header) {
                if (value.equals(variable)) {
                    return index;
                }
                index++;
            }

            System.out.println("\"" + variable + "\" was not found");

            System.exit(0);

            return -1;
        }

        /**
         *
         */
        public void join(Expression expression) throws Exception {
            WebService ws = WebServiceDescription.loadDescription(expression.function);

            System.out.println(expression.function);

            Map<String, Integer> parameters = new LinkedHashMap<>();

            List<Integer> escape = new LinkedList<>();

            for (int i = 0; i < ws.numberOfInputs; i++) {
                Expression.Element element = (Expression.Element) ((LinkedList) expression.elements).get(i);

                int index = -1;

                if (element.isVariable()) {
                    index = getIndexOfHeaderOrAlias(element.value);

                    escape.add(i);
                }

                parameters.put(element.value, index);

            }

            /*
            if (escape.isEmpty()) {
                System.out.println("Cartesian Product is not allowed");

                System.exit(0);
            }
            */
            
            List<Row> newRows = new LinkedList<>();

            for (Row currentRow : rows) {

                List<String> inputList = new LinkedList<>();

                for (Map.Entry<String, Integer> entry : parameters.entrySet()) {

                    String value = entry.getKey();

                    Integer index = entry.getValue();

                    if (index < 0) {
                        inputList.add(value);
                    } else {
                        inputList.add((String) ((LinkedList) currentRow.values).get(index));
                    }

                }

                String fileWithCallResult = ws.getCallResult(inputList);

                System.out.println("The call is: " + fileWithCallResult);

                String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);

                for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                    Row newRow = new Row(currentRow);
                    for (int i = 0; i < tuple.length; i++) {
                        if (!escape.contains(i)) {
                            newRow.values.add(tuple[i]);
                        }
                    }
                    newRows.add(newRow);
                }

            }

            for (Expression.Element element : expression.elements) {
                if (!header.contains(element.value)) {
                    header.add(element.value);
                }
            }

            rows = newRows;
        }

        /**
         *
         */
        public Relation projection(Expression expression) {
            Map<String, Integer> newHeaders = new LinkedHashMap<>();

            for (Expression.Element element : expression.elements) {
                newHeaders.put(element.value, getIndexOfHeaderOrAlias(element.value));
            }

            /*
            if (newHeaders.isEmpty()) {
                System.out.println("Empty projection is not allowed");

                System.exit(0);
            }

            if (newHeaders.size() < expression.elements.size()) {
                System.out.println("Constant projection is not allowed");

                System.exit(0);
            }
             */
            
            List<Row> newRows = new LinkedList<>();

            for (Row currentRow : rows) {
                Row newRow = new Row();
                for (Integer index : newHeaders.values()) {
                    newRow.values.add((String) ((LinkedList) currentRow.values).get(index));
                }
                newRows.add(newRow);
            }

            return new Relation(new LinkedList<String>(newHeaders.keySet()), newRows);
        }

        /**
         *
         */
        public void print() {
            System.out.println("The tuple results are:");

            System.out.print("(");
            for (int i = 0; i < header.size() - 1; i++) {
                System.out.print(((LinkedList) header).get(i) + "; ");
            }
            System.out.print(((LinkedList) header).get(header.size() - 1));
            System.out.print(")");
            System.out.println();

            for (Row row : rows) {
                System.out.print("(");
                for (int i = 0; i < row.values.size() - 1; i++) {
                    System.out.print(((LinkedList) row.values).get(i) + "; ");
                }
                System.out.print(((LinkedList) row.values).get(row.values.size() - 1));
                System.out.print(")");
                System.out.println();
            }
            System.out.println();
        }

        /**
         *
         */
        private static Relation getRelation(Expression expression) throws Exception {
            WebService ws = WebServiceDescription.loadDescription(expression.function);

            System.out.println(expression.function);

            String fileWithCallResult = ws.getCallResult(expression.getElementsAsListOfString());

            System.out.println("The call is: " + fileWithCallResult);

            String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);

            List<Row> rows = new LinkedList<>();

            for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                Row row = new Row();
                for (String value : tuple) {
                    row.values.add(value);
                }
                rows.add(row);
            }

            List<String> headers = new LinkedList<>();

            int inputs = 0;

            for (Expression.Element element : expression.elements) {
                if (element.isVariable()) {
                    headers.add(element.value);
                } else {
                    headers.add("_" + inputs++);
                }
            }

            return new Relation(headers, rows);
        }

    }

    /**
     *
     */
    public static final void main(String[] args) throws Exception {
        args = new String[1];

        //args[0] = "P(?title, ?year)<-mb_getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#mb_getAlbumByArtistId(?id, ?beginDate, ?aid, ?albumName)";
        //args[0] = "P(?albumName, ?beginDate)<-mb_getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#mb_getAlbumByArtistId(?id, ?r, ?aid, ?n)";
        //args[0] = "P(?n, ?b)<-mb_getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#mb_getAlbumByArtistId(?id, ?r, ?aid, ?n)";
        args[0] = "P(?n, ?b)<-mb_getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#mb_getAlbumByArtistId(?id, ?r, ?aid, ?n)";

        Map.Entry<Expression, List<Expression>> query = Expression.getQuery(args[0]);

        // Checking if the query is consistent
        /*        
        if (!Expression.isQueryConsitent(query)) {
            System.out.println("Query not well formed");

            System.exit(0);
        }
         */
        Expression.isQueryConsitent(query);

        Relation relation = null;

        for (Expression expression : query.getValue()) {

            if (relation == null) {
                relation = Relation.getRelation(expression);
            } else {
                relation.join(expression);
            }

            relation.print();

        }

        relation.projection(query.getKey()).print();

        /*
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
         */
    }

}
