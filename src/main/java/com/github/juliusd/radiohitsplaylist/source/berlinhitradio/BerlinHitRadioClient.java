package com.github.juliusd.radiohitsplaylist.source.berlinhitradio;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

interface BerlinHitRadioClient {

  @RequestLine("GET /services/program-info/history/rtl/{streamName}/0/19/56?items={items}")
  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0",
    "Accept: application/json, text/plain, */*",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
    "Cookie: sabcsid=2105216998",
    "Sec-Fetch-Dest: empty",
    "Sec-Fetch-Mode: cors",
    "Sec-Fetch-Site: same-origin",
  })
  List<BerlinHitRadioTrackWrapper> hitFinder(
    @Param("streamName") String streamName,
    @Param("items") int items);
//GET
//	https://www.radioteddy.de/services/program-info/history/radioteddy/teddy-live/0/17/19?items=48
}

