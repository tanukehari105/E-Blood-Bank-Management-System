package com.bloodbank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson configuration — ensures LocalDate and LocalTime are serialized
 * as plain strings (ISO format) rather than arrays, and can be deserialized
 * from both "HH:MM" and "HH:MM:SS" formats.
 */
@Configuration
public class JacksonConfig {

    // Accept both HH:MM and HH:MM:SS
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm[:ss]");

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule module = new JavaTimeModule();

        // LocalDate: serialize/deserialize as "2026-05-25"
        module.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        module.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));

        // LocalTime: serialize as "10:00", deserialize from "10:00" or "10:00:00"
        module.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm")));
        module.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(TIME_FORMAT));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
