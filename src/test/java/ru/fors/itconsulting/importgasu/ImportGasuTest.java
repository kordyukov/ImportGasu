package ru.fors.itconsulting.importgasu;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.fors.itconsulting.importgasu.config.DateLabelFormatter;
import ru.fors.itconsulting.importgasu.service.ImportGasuImpl;

import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Date;
import java.util.Properties;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

@SpringBootTest
public class ImportGasuTest {
    private final Properties properties = getProperties();
    private final JDatePickerImpl datePickerBegin = getJDatePickerImpl();
    private final JDatePickerImpl datePickerEnd = getJDatePickerImpl();

    @Autowired
    private ImportGasuImpl importGasu;


    @Test
    public void shouldRun() {
        importGasu.run(EMPTY_STRING);
    }

    @Test
    public void shouldImportFromDataBase() {
        importGasu.importFromDataBase(datePickerBegin, datePickerEnd);
    }

    private Properties getProperties() {
        var properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");

        return properties;
    }

    private JDatePickerImpl getJDatePickerImpl() {
        UtilDateModel model = new UtilDateModel();
        model.setValue(new Date());
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.setBackground(Color.GREEN);
        datePicker.setBorder(new BevelBorder(BevelBorder.LOWERED));

        return datePicker;
    }
}
