package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import com.github.juliusd.radiohitsplaylist.Track;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class BundesmuxLoader {

  private final BundesmuxClient bundesmuxClient;

  BundesmuxLoader(BundesmuxClient bundesmuxClient) {
    this.bundesmuxClient = bundesmuxClient;
  }

  public List<Track> load(String streamName) {
    String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);
    List<Track> tracks = IntStream.rangeClosed(5, 17).boxed().flatMap(page -> {
      var result = bundesmuxClient.load(streamName, yesterday, page);
      Document doc = Jsoup.parse(result);
      Elements playListEntry = doc.select("turbo-stream template > div > div");
      return playListEntry.stream().map((Element entry) -> {
        Elements divs = entry.select("div > div");
//        String date = divs.get(0).text().trim();
        String artist = divs.get(1).text().trim();
        String title = divs.get(2).text().trim();
//        System.out.println(date+") "+ artist+": "+title);
        return new Track(title, artist);
      });
    })
      .filter(track -> !track.title().equalsIgnoreCase("Coming Up"))
      .distinct().collect(toList());
    Collections.reverse(tracks);
    return Collections.unmodifiableList(tracks);
  }
}
