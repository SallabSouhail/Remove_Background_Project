package com.removebg.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "clerk")
public class ClerkProperties {
    private String issuer;
    private String jwksUrl;
    private long allowedClockSkewSeconds = 60L;
}
