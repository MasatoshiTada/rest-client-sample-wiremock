package com.example.restclientsample.config;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;

@TestConfiguration
public class TestConfig {
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    // HTTP/1.1にしないと、WireMockでエラーになるので設定
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            restClientBuilder.requestFactory(requestFactory);
        };
    }
}
