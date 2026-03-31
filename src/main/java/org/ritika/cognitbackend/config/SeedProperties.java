package org.ritika.cognitbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.seed")
@Data
public class SeedProperties {
    private boolean enabled = false;
    private int authors = 5;
    private int subscribers = 5;
    private int categories = 10;
    private int tags = 20;
    private int postsPerAuthor = 10;
}
