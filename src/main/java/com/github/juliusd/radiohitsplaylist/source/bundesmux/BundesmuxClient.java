package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface BundesmuxClient {

  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0",
    "Accept: text/vnd.turbo-stream.html",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
    "Sec-Fetch-Dest: empty",
    "Sec-Fetch-Mode: cors",
    "Sec-Fetch-Site: same-origin",
    "Priority: u=0"
  })
  @RequestLine("GET /playlistsuche?selected_station={selectedStation}&selected_date={selectedDate}&reload=0&page={page}")
  String load(
    @Param("selectedStation") String selectedStation,
    @Param("selectedDate") String selectedDate,
    @Param("page") int page
  );
}

