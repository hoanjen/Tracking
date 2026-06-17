package com.tracking.service;

import com.tracking.config.TikTokApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Fetches followers of a TikTok username via tiktok-scraper7 (RapidAPI).
 *
 * Flow:
 *   1. GET /user/info?unique_id={username}  → lấy user_id
 *   2. GET /user/followers?user_id={id}&count=200&max_time={cursor}
 *      Response: { has_more: true/false, max_time: <next_cursor>, total: N,
 *                  followers: [ {unique_id, ...}, ... ] }
 *      Dùng max_time từ response làm cursor cho request tiếp theo.
 */
@Service
@Slf4j
public class TikTokApiService {

    private static final String API_BASE = "https://tiktok-scraper7.p.rapidapi.com";
    private static final int PAGE_SIZE = 200;
    private static final int MAX_PAGES = 100;

    private final RestTemplate restTemplate;
    private final TikTokApiProperties properties;

    public TikTokApiService(RestTemplate restTemplate, TikTokApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Set<String> fetchFollowers(String username) {
        if (properties.getKey() == null || properties.getKey().isBlank()) {
            throw new RuntimeException("RAPIDAPI_KEY is not configured.");
        }

        UserInfo userInfo = fetchUserInfo(username);
        log.debug("Resolved @{} → user_id={}, followerCount={}", username, userInfo.userId(), userInfo.followerCount());

        Set<String> followers = fetchAllFollowers(userInfo.userId(), username, userInfo.followerCount());
        log.info("Total {} followers fetched for @{}", followers.size(), username);
        return followers;
    }

    private UserInfo fetchUserInfo(String username) {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String url = API_BASE + "/user/info?unique_id=" + encodedUsername;

        Map<?, ?> body = call(url);
        Map<?, ?> data = asMap(body.get("data"), "No data in /user/info response");
        Map<?, ?> user = asMap(data.get("user"), "User not found: @" + username);
        Map<?, ?> stats = asMap(data.get("stats"), "Stats missing in /user/info response");

        Object id = user.get("id");
        if (id == null) throw new RuntimeException("user_id missing in /user/info response");

        return new UserInfo(String.valueOf(id), toInt(stats.get("followerCount"), 0));
    }

    private Set<String> fetchAllFollowers(String userId, String username, int followerCount) {
        Set<String> followers = new LinkedHashSet<>();
        String cursor = "0";

        for (int page = 0; page < MAX_PAGES; page++) {
            String url = API_BASE + "/user/followers"
                    + "?user_id=" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
                    + "&count=" + PAGE_SIZE
                    + "&max_time=" + URLEncoder.encode(cursor, StandardCharsets.UTF_8);

            Map<?, ?> body = call(url);
            Map<?, ?> data = asMapOrNull(body.get("data"));
            if (data == null) break;

            List<?> list = firstList(data.get("followers"), data.get("user_list"), data.get("users"), data.get("list"));
            if (list == null || list.isEmpty()) break;

            int before = followers.size();
            for (Object item : list) {
                if (item instanceof Map<?, ?> user) {
                    Object uniqueId = firstNonNull(user.get("unique_id"), user.get("uniqueId"), user.get("username"));
                    if (uniqueId != null && !String.valueOf(uniqueId).isBlank()) {
                        followers.add(normalizeUsername(String.valueOf(uniqueId)));
                    }
                }
            }

            log.debug("Fetched {} followers for @{} (page={}, cursor={}, pageSize={})",
                    followers.size(), username, page + 1, cursor, list.size());

            if (followerCount > 0 && followers.size() >= followerCount) {
                break;
            }

            Object hasMoreObj = firstNonNull(data.get("has_more"), data.get("hasMore"), data.get("has_more_data"));
            boolean hasMore = Boolean.TRUE.equals(hasMoreObj)
                    || "1".equals(String.valueOf(hasMoreObj))
                    || "true".equalsIgnoreCase(String.valueOf(hasMoreObj));

            Object nextCursorObj = firstNonNull(
                    data.get("max_time"),
                    data.get("cursor"),
                    data.get("next_cursor"),
                    data.get("nextCursor"),
                    data.get("min_time"));
            String nextCursor = nextCursorObj != null ? String.valueOf(nextCursorObj) : null;

            if (nextCursor == null || nextCursor.isBlank() || nextCursor.equals(cursor) || nextCursor.equals("0")) {
                break;
            }

            if (!hasMore) {
                break;
            }

            cursor = nextCursor;
        }

        return followers;
    }

    private Map<?, ?> call(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", properties.getKey());
        headers.set("x-rapidapi-host", properties.getHost());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        } catch (Exception e) {
            log.error("TikTok API call failed [{}]: {}", url, e.getMessage());
            throw new RuntimeException("TikTok API call failed: " + e.getMessage());
        }

        Map<?, ?> body = response.getBody();
        if (body == null) throw new RuntimeException("Empty response from TikTok API");

        Object code = body.get("code");
        if (!"0".equals(String.valueOf(code))) {
            Object msgObj = body.get("msg");
            String msg = msgObj != null ? String.valueOf(msgObj) : "unknown error";
            throw new RuntimeException("TikTok API error: " + msg);
        }

        return body;
    }

    private String normalizeUsername(String username) {
        return username.trim().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
    }

    private Map<?, ?> asMap(Object value, String errorMessage) {
        Map<?, ?> map = asMapOrNull(value);
        if (map == null) throw new RuntimeException(errorMessage);
        return map;
    }

    private Map<?, ?> asMapOrNull(Object value) {
        return value instanceof Map<?, ?> map ? map : null;
    }

    private List<?> asListOrNull(Object value) {
        return value instanceof List<?> list ? list : null;
    }

    private List<?> firstList(Object... values) {
        for (Object value : values) {
            if (value instanceof List<?> list) return list;
        }
        return null;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record UserInfo(String userId, int followerCount) {}
}
