package com.andersoncarlosfs.execution;

import com.andersoncarlosfs.execution.parsers.ParseResultsForWS;
import com.andersoncarlosfs.execution.parsers.WebServiceDescription;
import com.andersoncarlosfs.execution.download.WebService;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                return null;
            }
            Expression head = getExpression(parts[0]);
            List<Expression> body = getListOfExpression(parts[1]);
            return head == null || body == null ? null : new AbstractMap.SimpleEntry(head, body);
        }

        /**
         *
         */
        private static boolean isQueryConsitent(Map.Entry<Expression, List<Expression>> query) {
            // Checking if the query is empty
            if (query == null) {
                return false;
            }
            /*
            Set<String> headConstants = new HashSet<>();
            Set<String> headVariables = new HashSet<>();
            for (Element element : query.getKey().elements) {
                if (element.isVariable()) {
                    headVariables.add(element.value);
                } else {
                    headConstants.add(element.value);
                }
            }
             */
            Set<String> bodyConstants = new HashSet<>();
            /*
            Set<String> bodyVariables = new HashSet<>();
             */
            for (Expression expression : query.getValue()) {
                for (Element element : expression.elements) {
                    if (element.isVariable()) {
                        /*
                        bodyVariables.add(element.value);
                         */
                    } else {
                        bodyConstants.add(element.value);
                    }
                }
            }
            return !query.getKey().elements.isEmpty() && !bodyConstants.isEmpty();
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

        private List<String> headers;
        private List<String> aliases;
        private List<Row> rows;

        public Relation() {
            this.headers = new LinkedList<>();
            this.aliases = new LinkedList<>();
            this.rows = new LinkedList<>();
        }

        public Relation(List<String> headers, List<String> aliases, List<Row> rows) {
            this.headers = headers;
            this.aliases = aliases;
            this.rows = rows;
        }

        /**
         *
         */
        private int getIndexOfHeaderOrAlias(Expression.Element element) {
            // Checking if the element is a variable    
            if (element.isVariable()) {
                return -1;
            }
            String variable = element.value;
            // Checking if the headers contain the variable    
            int index = 0;
            for (String value : headers) {
                if (value.equals(variable)) {
                    return index;
                }
                index++;
            }
            // Checking if the aliases contain the variable            
            index = 0;
            for (String value : aliases) {
                if (value.equals(variable)) {
                    return index;
                }
                index++;
            }

            System.out.println("Query not well formed");

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

            int variables = 0;

            for (int i = 0; i < ws.numberOfInputs; i++) {
                Expression.Element element = (Expression.Element) ((LinkedList) expression.elements).get(i);

                Integer index = getIndexOfHeaderOrAlias(element);

                parameters.put(element.value, index);

                variables += index;
            }

            if (variables <= -parameters.size()) {
                System.out.println("Cartesian Product is not allowed");

                System.exit(0);
            }

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

                String fileWithCallResult = ws.getCallResult(expression.getElementsAsListOfString());

                System.out.println("The call is: " + fileWithCallResult);

                String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);

                for (String[] tuple : ParseResultsForWS.showResults(fileWithTransfResults, ws)) {
                    Row newRow = new Row(currentRow);
                    for (int i = 0; i < tuple.length; i++) {
                        if (!parameters.containsValue(i)) {
                            newRow.values.add(tuple[i]);
                        }
                    }
                    newRows.add(newRow);
                }

            }

            for (int i = 0; i < ws.headVariables.size(); i++) {

                String header = ws.headVariables.get(i);

                if (!headers.contains(header)) {

                    Expression.Element element = (Expression.Element) ((LinkedList) expression.elements).get(i);

                    if (element.isVariable()) {
                        aliases.add(element.value);
                    } else {
                        aliases.add("");
                    }

                    headers.add(header);

                }

            }

            rows = newRows;

        }

        /**
         *
         */
        public void print() {
            System.out.println("The tuple results are:");
            for (Row row : rows) {
                System.out.print("(");
                for (int i = 0; i < row.values.size() - 1; i++) {
                    System.out.print(((LinkedList) row.values).get(i) + ", ");
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

            List<String> aliases = new LinkedList<>();

            for (Expression.Element element : expression.elements) {
                if (element.isVariable()) {
                    aliases.add(element.value);
                } else {
                    aliases.add("");
                }
            }

            List<String> headers = ws.headVariables;

            return new Relation(headers, aliases, rows);
        }

    }

    /**
     *
     */
    public static final void main(String[] args) throws Exception {
        args = new String[1];

        args[0] = "P(?title, ?year)<-mb_getArtistInfoByName(Frank Sinatra, ?id, ?b, ?e)#mb_getAlbumByArtistId(?id, ?aid, ?albumName)";

        Map.Entry<Expression, List<Expression>> query = Expression.getQuery(args[0]);

        if (!Expression.isQueryConsitent(query)) {

            System.out.println("Query not well formed");

            System.exit(0);

        }

        Relation relation = null;

        for (Expression expression : query.getValue()) {

            if (relation == null) {
                relation = Relation.getRelation(expression);
            } else {
                relation.join(expression);
            }

            relation.print();

        }

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
