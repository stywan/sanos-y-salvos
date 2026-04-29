package cl.duocuc.sanossalvos.matchingengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("petManagementRestClient")
    public RestClient petManagementRestClient(
            @Value("${services.pet-management.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean("geolocationRestClient")
    public RestClient geolocationRestClient(
            @Value("${services.geolocation.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean("notificationRestClient")
    public RestClient notificationRestClient(
            @Value("${services.notification.url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
