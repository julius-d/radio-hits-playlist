package com.github.juliusd.radiohitsplaylist.source.family;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

interface FamilyRadioClient {

  @RequestLine("GET /services/program-info/history/radioteddy/{streamName}/0/19/56?items={items}")
  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0",
    "Accept: application/json, text/plain, */*",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
    "Cookie: fe_typo_user=9b235ab37b355c3ba7a356a147a59906; sabcsid=2852929066",
    "Sec-Fetch-Dest: empty",
    "Sec-Fetch-Mode: cors",
    "Sec-Fetch-Site: same-origin",
  })
  List<FamilyTrackWrapper> hitFinder(
    @Param("streamName") String streamName,
    @Param("items") int items);
//GET
//	https://www.radioteddy.de/services/program-info/history/radioteddy/teddy-live/0/17/19?items=48
}

