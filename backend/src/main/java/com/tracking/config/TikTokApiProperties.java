package com.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds tiktok.rapidapi.* from application.yml.
 *
 * application.yml:
 *   tiktok:
 *     rapidapi:
 *       key: <your-key>
 *       host: tiktok-scraper7.p.rapidapi.com
 */
@ConfigurationProperties(prefix = "tiktok.rapidapi")
public class TikTokApiProperties {

    private String key;
    private String host;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
