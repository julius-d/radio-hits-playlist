package com.github.juliusd.radiohitsplaylist.source.bundesmux;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface BundesmuxClient {

  @RequestLine(
      "GET /_server/?id=src_api_cms_queries_metadataHistoryQuery_ts--getMetadataHistoryQuery_query"
          + "&name=%2Fapp%2Fsrc%2Fapi%2Fcms%2Fqueries%2FmetadataHistoryQuery.ts%3Ftsr-directive-use-server%3D&args={args}")
  @Headers({
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0",
    "Accept: */*",
    "Accept-Language: de,en-US;q=0.7,en;q=0.3",
    "Sec-Fetch-Dest: empty",
    "Sec-Fetch-Mode: cors",
    "Sec-Fetch-Site: same-origin",
    "Priority: u=0",
    "x-server-instance: server-fn:13"
  })
  String getMetadataHistory(@Param("args") String args);
}
