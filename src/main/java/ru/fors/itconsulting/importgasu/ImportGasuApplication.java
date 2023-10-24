package ru.fors.itconsulting.importgasu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.fors.itconsulting.importgasu.service.ImportGasu;

import java.awt.*;

@SpringBootApplication
public class ImportGasuApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ImportGasuApplication.class).headless(false).run(args);
        EventQueue.invokeLater(() -> {
            ImportGasu appFrame = context.getBean(ImportGasu.class);
            appFrame.setVisible(false);
        });
    }

}
