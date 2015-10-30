package com.gs.api.search.util;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class HttpRequestBuilder {

    @Value("${search.solr.credentials}")
    private String solrCredentials;

    /**
     * Create the Request Header for the Solr using the solr credentials
     */
    public  HttpEntity<String> createRequestHeader() {
        // create request header contain basic auth credentials
        byte[] plainCredsBytes = solrCredentials.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        return new HttpEntity<String>(headers);
    }

}
