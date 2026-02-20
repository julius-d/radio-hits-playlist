package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Utility class for encoding Bundesmux API args parameter. */
public class BundesmuxArgsEncoder {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String buildArgs(String channelIdentifier, Instant start, int page, int limit) {
    try {
      String startISO = start.toString();
      ObjectNode root = objectMapper.createObjectNode();
      ObjectNode t = objectMapper.createObjectNode();

      t.put("t", 9);
      t.put("i", 0);
      t.put("l", 1);

      ArrayNode a = objectMapper.createArrayNode();
      ObjectNode entry = objectMapper.createObjectNode();
      entry.put("t", 10);
      entry.put("i", 1);

      ObjectNode p = objectMapper.createObjectNode();
      ArrayNode k = objectMapper.createArrayNode();
      k.add("channelIdentifier");
      k.add("start");
      k.add("page");
      k.add("limit");

      ArrayNode v = objectMapper.createArrayNode();

      // channelIdentifier - type 1 (string)
      ObjectNode channelValue = objectMapper.createObjectNode();
      channelValue.put("t", 1);
      channelValue.put("s", channelIdentifier);
      v.add(channelValue);

      // start - type 1 (string)
      ObjectNode startValue = objectMapper.createObjectNode();
      startValue.put("t", 1);
      startValue.put("s", startISO);
      v.add(startValue);

      // page - type 0 (number)
      ObjectNode pageValue = objectMapper.createObjectNode();
      pageValue.put("t", 0);
      pageValue.put("s", page);
      v.add(pageValue);

      // limit - type 0 (number)
      ObjectNode limitValue = objectMapper.createObjectNode();
      limitValue.put("t", 0);
      limitValue.put("s", limit);
      v.add(limitValue);

      p.set("k", k);
      p.set("v", v);
      p.put("s", 4);
      entry.set("p", p);
      entry.put("o", 0);

      a.add(entry);
      t.set("a", a);
      t.put("o", 0);
      root.set("t", t);
      root.put("f", 31);
      root.set("m", objectMapper.createArrayNode());

      String json = objectMapper.writeValueAsString(root);
      return URLEncoder.encode(json, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to build args parameter", e);
    }
  }
}
