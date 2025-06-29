package com.github.juliusd.radiohitsplaylist.source.youngpeople;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface YoungPeopleClient {

  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0",
    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
    "Cache-Control: max-age=0"
  })
  @RequestLine("GET /programm/sendungen/{programName}.html")
  String getPlaylistHtml(@Param("programName") String programName);
}
