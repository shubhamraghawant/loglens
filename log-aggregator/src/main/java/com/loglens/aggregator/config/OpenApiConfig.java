package com.loglens.aggregator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logAggregatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogLens Log Aggregator API")
                        .description("Kafka consumer service that persists logs and fires threshold-based alerts")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LogLens Team")
                                .email("shubhamraghawant@gmail.com")));
    }
}
