package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for extracting JSON from JavaScript-wrapped Bundesmux API responses. */
public class BundesmuxResponseExtractor {

  private static final Pattern SUCCESS_OBJECT_PATTERN =
      Pattern.compile("\\$R\\[0\\]=\\{success:.*");

  /**
   * Extracts JSON from a JavaScript-wrapped response. Handles JavaScript object notation by
   * converting unquoted field names to quoted JSON format. Also handles JavaScript boolean syntax
   * (!0 = true, !1 = false) and removes $R[n]= assignments.
   *
   * @param jsResponse the JavaScript response string
   * @return the extracted JSON string
   * @throws IllegalArgumentException if JSON cannot be extracted
   */
  public static String extractJson(String jsResponse) {
    if (jsResponse == null || jsResponse.isEmpty()) {
      throw new IllegalArgumentException("Response is null or empty");
    }

    // Find the $R[0]={success:... part
    Matcher objectMatcher = SUCCESS_OBJECT_PATTERN.matcher(jsResponse);
    if (!objectMatcher.find()) {
      throw new IllegalArgumentException("Could not find JSON in response");
    }

    String extracted = objectMatcher.group(0);

    // Remove the $R[0]= prefix
    if (extracted.startsWith("$R[0]=")) {
      extracted = extracted.substring(6);
    }

    // Find the last closing brace and trim everything after it
    int lastBraceIndex = extracted.lastIndexOf('}');
    if (lastBraceIndex > 0) {
      extracted = extracted.substring(0, lastBraceIndex + 1);
    }

    // Remove all remaining $R[n]= assignments
    extracted = extracted.replaceAll("\\$R\\[\\d+\\]=", "");

    // Replace JavaScript booleans with JSON booleans
    extracted = extracted.replace(":!0", ":true");
    extracted = extracted.replace(":!1", ":false");
    extracted = extracted.replace(",!0", ",true");
    extracted = extracted.replace(",!1", ",false");

    // Quote unquoted field names
    // We need to be careful not to quote things inside string values
    StringBuilder result = new StringBuilder();
    boolean inString = false;
    boolean escapeNext = false;

    for (int i = 0; i < extracted.length(); i++) {
      char c = extracted.charAt(i);

      if (escapeNext) {
        result.append(c);
        escapeNext = false;
        continue;
      }

      if (c == '\\') {
        result.append(c);
        escapeNext = true;
        continue;
      }

      if (c == '"') {
        inString = !inString;
        result.append(c);
        continue;
      }

      if (!inString && Character.isLetter(c)) {
        // Start of a potential field name
        int j = i;
        while (j < extracted.length()
            && (Character.isLetterOrDigit(extracted.charAt(j)) || extracted.charAt(j) == '_')) {
          j++;
        }

        if (j < extracted.length() && extracted.charAt(j) == ':') {
          // This is a field name
          String fieldName = extracted.substring(i, j);
          if (!fieldName.equals("true")
              && !fieldName.equals("false")
              && !fieldName.equals("null")) {
            result.append('"').append(fieldName).append('"');
            i = j - 1; // -1 because loop will increment
            continue;
          }
        }
      }

      result.append(c);
    }

    return result.toString();
  }
}
