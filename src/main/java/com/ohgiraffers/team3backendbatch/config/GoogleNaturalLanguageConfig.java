package com.ohgiraffers.team3backendbatch.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

@Configuration
public class GoogleNaturalLanguageConfig {

    @Bean(destroyMethod = "close")
    public LanguageServiceClient languageServiceClient(
        @Value("${nlp.google.credentials-location:}") String credentialsLocation,
        ResourceLoader resourceLoader
    ) throws IOException {
        LanguageServiceSettings.Builder settingsBuilder = LanguageServiceSettings.newBuilder();

        if (StringUtils.hasText(credentialsLocation)) {
            Resource resource = resolveResource(credentialsLocation, resourceLoader);
            try (InputStream inputStream = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
                settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
            }
        }

        return LanguageServiceClient.create(settingsBuilder.build());
    }

    private Resource resolveResource(String credentialsLocation, ResourceLoader resourceLoader) {
        if (credentialsLocation.startsWith("classpath:") || credentialsLocation.startsWith("file:")) {
            return resourceLoader.getResource(credentialsLocation);
        }
        return resourceLoader.getResource("file:" + credentialsLocation);
    }
}