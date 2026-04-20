package com.acme.security.oauth.in.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean("oauth2ObjectMapper") // Nomeie o bean explicitamente
    public ObjectMapper oauth2ObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = JacksonConfig.class.getClassLoader();
        mapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        mapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        return mapper;
    }
}