package com.ohgiraffers.team3backendbatch.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.NlpAnalysisGateway;
import com.ohgiraffers.team3backendbatch.infrastructure.nlp.google.GoogleNaturalLanguageGateway;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class GoogleNaturalLanguageConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${nlp.google.credentials-location:}')")
    public LanguageServiceClient languageServiceClient(
        @Value("${nlp.google.credentials-location:}") String credentialsLocation,
        ResourceLoader resourceLoader
    ) throws IOException {
        LanguageServiceSettings.Builder settingsBuilder = LanguageServiceSettings.newBuilder();

        Resource resource = resolveResource(credentialsLocation, resourceLoader);
        try (InputStream inputStream = resource.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
            settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
        }

        return LanguageServiceClient.create(settingsBuilder.build());
    }

    @Bean
    @ConditionalOnBean(LanguageServiceClient.class)
    public NlpAnalysisGateway googleNaturalLanguageGateway(LanguageServiceClient languageServiceClient) {
        return new GoogleNaturalLanguageGateway(languageServiceClient);
    }

    @Bean
    @ConditionalOnMissingBean(NlpAnalysisGateway.class)
    public NlpAnalysisGateway disabledNlpAnalysisGateway() {
        return text -> {
            throw new IllegalStateException(
                "Google NLP is not configured. Set nlp.google.credentials-location to run qualitative analysis."
            );
        };
    }

    private Resource resolveResource(String credentialsLocation, ResourceLoader resourceLoader) {
        if (credentialsLocation.startsWith("classpath:") || credentialsLocation.startsWith("file:")) {
            return resourceLoader.getResource(credentialsLocation);
        }
        return resourceLoader.getResource("file:" + credentialsLocation);
    }
}