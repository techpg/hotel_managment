package com.techpg.hotelbooking.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    // Domain types (Property, RoomType, Booking, OwnerAccount) are plain immutable
    // classes with record-style accessors (id(), name(), ...) rather than getX()/isX().
    // Reading fields directly lets Jackson serialize them without coupling the domain
    // model to Jackson's bean-getter naming convention.
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer fieldVisibilityCustomizer() {
        return builder -> builder
                .visibility(PropertyAccessor.FIELD, Visibility.ANY)
                .visibility(PropertyAccessor.GETTER, Visibility.NONE)
                .visibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
    }
}
