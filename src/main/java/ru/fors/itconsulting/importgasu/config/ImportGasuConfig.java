package ru.fors.itconsulting.importgasu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.fors.itconsulting.importgasu.utils.Console;

import java.util.Properties;

@Configuration
public class ImportGasuConfig {
    @Bean
    public Properties properties() {
        var properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");

        return properties;
    }

    @Bean
    public Console console() {
        return new Console();
    }
}
