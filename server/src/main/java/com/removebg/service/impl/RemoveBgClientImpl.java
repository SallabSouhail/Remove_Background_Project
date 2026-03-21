package com.removebg.service.impl;

import com.removebg.service.RemoveBgClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoveBgClientImpl implements RemoveBgClient {

    private final WebClient removeBgWebClient;

    @Override
    public byte[] removeBackground(MultipartFile image) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("size", "auto");

        ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image";
            }
        };

        body.add("image_file", imageResource);

        try {
            return removeBgWebClient.post()
                    .uri("/removebg")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("remove.bg API returned status {} for {}", ex.getStatusCode(), image.getOriginalFilename());
            throw ex;
        } catch (RuntimeException ex) {
            log.error("remove.bg API call failed for {}", image.getOriginalFilename(), ex);
            throw ex;
        }
    }
}
