package com.github.juliusd.radiohitsplaylist.source.youngpeople;

import com.github.juliusd.radiohitsplaylist.Track;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class YoungPeopleLoader {

  private final YoungPeopleClient youngPeopleClient;

  YoungPeopleLoader(YoungPeopleClient youngPeopleClient) {
    this.youngPeopleClient = youngPeopleClient;
  }

  public List<Track> load(String programName) {
    String html = youngPeopleClient.getPlaylistHtml(programName);
    Document document = Jsoup.parse(html);

    Elements trackRows = document.select("tr.play_track");
    List<Track> tracks = new java.util.ArrayList<>();

    for (Element row : trackRows) {
      Element interpretElement = row.selectFirst("td.trackinterpret");
      Element titleElement = row.selectFirst("td.tracktitle");

      if (interpretElement != null && titleElement != null) {
        String artist = interpretElement.text().trim();
        String title = titleElement.text().trim();

        if (!artist.isEmpty() && !title.isEmpty()) {
          tracks.add(new Track(title, artist));
        }
      }
    }

    return tracks.stream().distinct().toList();
  }
}
