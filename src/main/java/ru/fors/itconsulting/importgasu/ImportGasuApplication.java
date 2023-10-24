package ru.fors.itconsulting.importgasu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.fors.itconsulting.importgasu.service.ImportGasuImpl;

import java.awt.*;

@SpringBootApplication
public class ImportGasuApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ImportGasuApplication.class).headless(false).run(args);
        EventQueue.invokeLater(() -> {
            ImportGasuImpl appFrame = context.getBean(ImportGasuImpl.class);
            appFrame.setVisible(false);
        });
    }

}
