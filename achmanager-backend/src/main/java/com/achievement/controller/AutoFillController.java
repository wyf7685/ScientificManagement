package com.achievement.controller;

import com.achievement.config.EasyScholarProperties;
import com.achievement.result.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

@Slf4j
@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
@Tag(name = "智能补全接口")
public class AutoFillController {
    private final EasyScholarProperties props;
    private final ObjectMapper objectMapper;
    private static final int MAX_REQUESTS_PER_SECOND = 2;
    private static final long RATE_WINDOW_MS = 1000L;
    private static final Deque<Long> REQUEST_TIMES = new ArrayDeque<>();

    @Operation(description = "智能补全（期刊等级）")
    @GetMapping("/auto-fill")
    public Result<JsonNode> autoFill(
            @RequestParam String type,
            @RequestParam String value
    ) {
        if (!"journalRank".equalsIgnoreCase(type)) {
            return Result.error("暂不支持该补全类型");
        }
        if (!StringUtils.hasText(value)) {
            return Result.error("期刊名称不能为空");
        }
        if (!StringUtils.hasText(props.getSecretKey())) {
            return Result.error("缺少 EASYSCHOLAR_SECRET_KEY 配置");
        }

        try {
            if (!allowRequest()) {
                return Result.error("请求过于频繁，请稍后重试");
            }
            String encodedName = URLEncoder.encode(value, StandardCharsets.UTF_8);
            URI uri = UriComponentsBuilder.fromHttpUrl(props.getBaseUrl())
                    .queryParam("secretKey", props.getSecretKey())
                    .queryParam("publicationName", encodedName)
                    .build(true)
                    .toUri();

            RestTemplate restTemplate = new RestTemplate();
            String raw = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(raw == null ? "{}" : raw);
            int code = root.path("code").asInt(-1);
            if (code != 200) {
                String msg = root.path("msg").asText("查询失败");
                return Result.error(msg);
            }
            JsonNode data = root.path("data");
            return Result.success(data);
        } catch (Exception e) {
            log.error("Auto fill failed", e);
            return Result.error("补全失败，请稍后重试");
        }
    }

    private boolean allowRequest() {
        long now = System.currentTimeMillis();
        synchronized (REQUEST_TIMES) {
            while (!REQUEST_TIMES.isEmpty() && now - REQUEST_TIMES.peekFirst() > RATE_WINDOW_MS) {
                REQUEST_TIMES.pollFirst();
            }
            if (REQUEST_TIMES.size() >= MAX_REQUESTS_PER_SECOND) {
                return false;
            }
            REQUEST_TIMES.addLast(now);
            return true;
        }
    }
}
