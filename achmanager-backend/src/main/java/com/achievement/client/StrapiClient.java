package com.achievement.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StrapiClient {

    private final WebClient strapiWebClient;


    //新增成果物类型，只对成果物类型表进行操作

    // ✅ 新增：POST /api/{collection}  body: {"data":{...}}
    public String create(String collection, Map<String, Object> data) {
        return strapiWebClient.post()
                .uri("/api/{collection}", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Strapi error: " + err)))
                )
                .bodyToMono(String.class)
                .block();
    }

    // ✅更新：PUT /api/{collection}/{id}  body: {"data":{...}}
    public String update(String collection, Object id, Map<String, Object> data) {
        return strapiWebClient.put()
                .uri("/api/{collection}/{id}", collection, id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Strapi error: " + err)))
                )
                .bodyToMono(String.class)
                .block();
    }

    // ✅ 上传文件：POST /api/upload (multipart/form-data, field name: files)
    // GET /api/{collection}?...
    public String query(String collection, Map<String, String> queryParams) {
        Map<String, String> params = queryParams == null ? Map.of() : new LinkedHashMap<>(queryParams);
        return strapiWebClient.get()
                .uri(uriBuilder -> buildCollectionUri(uriBuilder, collection, params))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Strapi error: " + err)))
                )
                .bodyToMono(String.class)
                .block();
    }

    private URI buildCollectionUri(UriBuilder uriBuilder, String collection, Map<String, String> queryParams) {
        UriBuilder builder = uriBuilder.path("/api/{collection}");
        for (Map.Entry<String, String> e : queryParams.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            builder = builder.queryParam(e.getKey(), e.getValue());
        }
        return builder.build(collection);
    }

    public String upload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return "[]";
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            ByteArrayResource resource;
            try {
                resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
            } catch (IOException e) {
                throw new RuntimeException("Failed to read upload file bytes", e);
            }

            builder.part("files", resource)
                    .filename(file.getOriginalFilename())
                    .contentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(file.getContentType()));
        }

        MultiValueMap<String, HttpEntity<?>> multipartData = builder.build();

        return strapiWebClient.post()
                .uri("/api/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(multipartData))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Strapi error: " + err)))
                )
                .bodyToMono(String.class)
                .block();
    }
    // ✅ 查询单条：GET /api/{collection}/{id}
    public String findOne(String collection, Object id, Map<String, String> queryParams) {
        Map<String, String> params = queryParams == null ? Map.of() : new LinkedHashMap<>(queryParams);

        return strapiWebClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder.path("/api/{collection}/{id}");
                    for (Map.Entry<String, String> e : params.entrySet()) {
                        if (e.getKey() == null || e.getValue() == null) continue;
                        builder = builder.queryParam(e.getKey(), e.getValue());
                    }
                    return builder.build(collection, id);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Strapi error: " + err)))
                )
                .bodyToMono(String.class)
                .block();
    }


}
