package com.removebg.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClerkJwksProvider {

    private static final long CACHE_TTL_MS = 3_600_000L;

    private final ClerkProperties clerkProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile long lastFetchTime = 0L;

    public PublicKey getPublicKey(String kid) throws Exception {
        if (isCacheFresh() && keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }

        synchronized (this) {
            if (!isCacheFresh() || !keyCache.containsKey(kid)) {
                refreshKeys();
            }
        }

        PublicKey publicKey = keyCache.get(kid);
        if (publicKey == null) {
            throw new IllegalArgumentException("Unable to find Clerk public key for kid " + kid);
        }

        return publicKey;
    }

    public void refreshKeys() throws Exception {
        String response = restTemplate.getForObject(clerkProperties.getJwksUrl(), String.class);
        JsonNode jwks = objectMapper.readTree(response);
        JsonNode keys = jwks.get("keys");
        if (keys == null || !keys.isArray()) {
            throw new IllegalArgumentException("Clerk JWKS response did not contain a valid keys array.");
        }

        Map<String, PublicKey> refreshedKeys = new ConcurrentHashMap<>();
        for (JsonNode key : keys) {
            String kid = key.get("kid").asText();
            String kty = key.get("kty").asText();
            String alg = key.get("alg").asText();

            if ("RSA".equals(kty) && "RS256".equals(alg)) {
                String n = key.get("n").asText();
                String e = key.get("e").asText();
                refreshedKeys.put(kid, createPublicKey(n, e));
            }
        }

        keyCache.clear();
        keyCache.putAll(refreshedKeys);
        lastFetchTime = System.currentTimeMillis();
        log.debug("Refreshed {} Clerk JWKS keys", keyCache.size());
    }

    private boolean isCacheFresh() {
        return System.currentTimeMillis() - lastFetchTime < CACHE_TTL_MS;
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {
        byte[] decodedModulus = Base64.getUrlDecoder().decode(modulus);
        byte[] decodedExponent = Base64.getUrlDecoder().decode(exponent);

        BigInteger n = new BigInteger(1, decodedModulus);
        BigInteger e = new BigInteger(1, decodedExponent);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
