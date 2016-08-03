package com.caseybrooks.common.util.clog;

import com.caseybrooks.common.util.clog.formatters.ClogTimestamp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Clog {
    static HashMap<String, ClogFormatter> formatters = new HashMap<>();
    static {
        formatters = new HashMap<>();
        formatters.put("timestamp", new ClogTimestamp());
    }

    public static String formatString(String message, Object... params) {
        if(params != null && params.length > 0) {

            String replacementRegex = "\\{" + "\\{" + "([^\\{}]*)" + "\\}" + "\\}";
            Pattern pattern = Pattern.compile(replacementRegex);
            Matcher matcher = pattern.matcher(message);

            int lastIndex = 0;
            String output = "";
            while(matcher.find()) {
                // Add all text that isn't part of the formatter pieces
                String formatBody = message.substring(lastIndex, matcher.start());
                output += formatBody;

                // Split inner string on '|'. The first piece should indicate which object from the
                // params we should start with, and the other pieces should create a pipeline of
                // ClogFormatters which continually format the object.
                String token = matcher.group(1).trim();
                String[] bodyPieces = token.split("\\|");
                Object objectToPrint = getObjectToFormat(bodyPieces[0].trim(), params);
                if(bodyPieces.length > 1) {
                    output += formatObject(objectToPrint, bodyPieces).toString();
                }
                else {
                    output += objectToPrint.toString();
                }

                lastIndex = matcher.end();
            }

            output += message.substring(lastIndex, message.length());

            return output;
        }
        else {
            return message;
        }
    }

    private static Object getObjectToFormat(String indexPiece, Object[] params) {
        if(indexPiece.matches("^\\$\\d+$")) {
            int objectIndex = Integer.parseInt(indexPiece.substring(1)) - 1;

            if(objectIndex >= 0 && objectIndex < params.length) {
                return params[objectIndex];
            }
            else if(objectIndex == -1) {
                return null;
            }
            else {
                throw new IllegalArgumentException("Attempted to access an object not within the range of given params: Object index: " + objectIndex + ", number of params: " + params.length);
            }
        }
        else {
            throw new IllegalArgumentException("Formatters must specify an object to format using the format '$index': '" + indexPiece + "'");
        }
    }

    private static Object formatObject(Object objectToPrint, String[] formatterPieces) {
        String[] formatterKeys = Arrays.copyOfRange(formatterPieces, 1, formatterPieces.length);

        Object formattedObject = objectToPrint;
        for(String formatterKey : formatterKeys) {
            formatterKey = formatterKey.trim();
            String[] paramsArray = null;

            // Get optional params for the formatter in they exist
            Pattern pattern = Pattern.compile("\\((.*)\\)");
            Matcher matcher = pattern.matcher(formatterKey);
            if(matcher.find()) {
                String paramsString = matcher.group(1);

                if(paramsString.contains(",")) {
                    paramsArray = paramsString.split("\\s*,\\s*");
                }
                else {
                    paramsArray = new String[] { paramsString };
                }

                formatterKey = formatterKey.replaceAll("\\((.*)\\)", "").trim();
            }

            if(formatters.containsKey(formatterKey)) {
                formattedObject = formatters.get(formatterKey).format(formattedObject, paramsArray);
            }
            else {
                throw new IllegalArgumentException("Cannot find the formatter with key '" + formatterKey + "'");
            }
        }

        return formattedObject;
    }
}
