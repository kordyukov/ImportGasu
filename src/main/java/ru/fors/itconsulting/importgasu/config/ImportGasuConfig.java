package ru.fors.itconsulting.importgasu.config;

import com.spire.data.table.DataTable;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Workbook workbook() {
        return new Workbook();
    }

    @Bean
    public Worksheet worksheet() {
        return workbook().getWorksheets().get(0);
    }

    @Bean
    public DataTable dataTable() {
        return new DataTable();
    }
}
