package com.loglens.producer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logProducerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogLens Log Producer API")
                        .description("REST API for emitting logs and controlling the log simulation engine")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LogLens Team")
                                .email("shubhamraghawant@gmail.com")));
    }
}
