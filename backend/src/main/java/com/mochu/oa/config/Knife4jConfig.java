package com.mochu.oa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MOCHU-OA 接口文档")
                        .description("墨初OA施工管理系统 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MOCHU")
                                .email("support@mochu.com")));
    }
}