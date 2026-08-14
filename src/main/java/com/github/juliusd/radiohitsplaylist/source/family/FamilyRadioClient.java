package com.github.juliusd.radiohitsplaylist.source.family;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface FamilyRadioClient {

  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0",
    "Accept: application/json, text/plain, */*",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
  })
  @RequestLine("GET /ctrl-api/getPlaylist?k={channelKey}&typ=hour&ts={ts}")
  FamilyRadioResponse getPlaylist(@Param("channelKey") String channelKey, @Param("ts") long ts);
}
