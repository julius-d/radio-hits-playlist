package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Utility class for normalizing text, particularly converting all-caps text to title case. */
public class TextNormalizer {

  /**
   * Normalizes text if it's all uppercase by converting it to title case. If the text is not all
   * uppercase, it's returned unchanged.
   *
   * @param text the text to normalize
   * @return normalized text in title case if originally all caps, otherwise unchanged
   */
  public static String normalizeIfAllCaps(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    // Check if text is all uppercase (ignoring spaces and special characters)
    if (!isAllUpperCase(text)) {
      return text;
    }

    return toTitleCase(text);
  }

  private static boolean isAllUpperCase(String text) {
    for (char c : text.toCharArray()) {
      if (Character.isLetter(c) && !Character.isUpperCase(c)) {
        return false;
      }
    }
    return true;
  }

  private static String toTitleCase(String text) {
    String[] words = text.split("\\s+");
    return Arrays.stream(words)
        .map(TextNormalizer::capitalizeWord)
        .collect(Collectors.joining(" "));
  }

  private static String capitalizeWord(String word) {
    if (word.isEmpty()) {
      return word;
    }

    // Handle special cases
    if (word.equalsIgnoreCase("FEAT.") || word.equalsIgnoreCase("FEAT")) {
      return "feat.";
    }
    if (word.equals("&")) {
      return "&";
    }

    // Handle words with apostrophes (e.g., "DON'T" -> "Don't")
    if (word.contains("'")) {
      int apostropheIndex = word.indexOf("'");
      String beforeApostrophe = word.substring(0, apostropheIndex);
      String afterApostrophe = word.substring(apostropheIndex);
      return capitalizeSimpleWord(beforeApostrophe) + afterApostrophe.toLowerCase();
    }

    return capitalizeSimpleWord(word);
  }

  private static String capitalizeSimpleWord(String word) {
    if (word.isEmpty()) {
      return word;
    }
    return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
  }
}
