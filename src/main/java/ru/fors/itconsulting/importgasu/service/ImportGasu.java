package ru.fors.itconsulting.importgasu.service;

import com.spire.data.table.DataTable;
import com.spire.data.table.common.JdbcAdapter;
import com.spire.xls.ExcelVersion;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.fors.itconsulting.importgasu.config.DateLabelFormatter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import static ru.fors.itconsulting.importgasu.constant.ImportGasuConstant.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class ImportGasu extends JFrame implements CommandLineRunner {
    private final Properties properties;
    private final Workbook workbook;
    private final Worksheet sheet;
    private final DataTable dataTable;
    private final StringWriter writer;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${files.output-file-name}")
    private String outputFileName;
    @Value("${files.input-sql-query}")
    private String inputSqlQuery;

    @Override
    public void run(String... arg0) {
        EventQueue.invokeLater(() -> {
            JPanel databaseAddres = new JPanel();
            JLabel urlText = new JLabel("Адрес базы: " + url);

            var importButton = new JButton("Импорт");
            var quitButton = new JButton("Закрыть");

            JDatePickerImpl datePickerBegin = getJDatePickerImpl();
            JDatePickerImpl datePickerEnd = getJDatePickerImpl();

            importButton.addActionListener((ActionEvent event) -> importFromDataBase(datePickerBegin, datePickerEnd));

            quitButton.addActionListener((ActionEvent event) -> System.exit(0));

            JFrame window = new JFrame("ImportGasu приложение для импорта, база: " + url);
            window.setBounds(50, 50, 1000, 150);
            window.setVisible(true);
            window.setResizable(false);
            window.setBackground(Color.GREEN);


            databaseAddres.add(urlText);
            window.add(databaseAddres, BorderLayout.LINE_START);

            JPanel panelPeriods = new JPanel();
            panelPeriods.add(importButton);
            panelPeriods.add(new JLabel("Выберите периоды: "));
            panelPeriods.add(datePickerBegin);
            panelPeriods.add(datePickerEnd);
            panelPeriods.add(quitButton);

            JPanel panelActions = new JPanel();
            window.add(panelPeriods, BorderLayout.AFTER_LAST_LINE);
            window.add(panelActions, BorderLayout.CENTER);
        });
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

    public void importFromDataBase(JDatePickerImpl datePickerBegin, JDatePickerImpl datePickerEnd) {
        try {
            log.info("Начало извлечения заявлений из ГАСУв файл с программой в каталог output в файл " + outputFileName);

            String selectedDateBegin = convertDateToDateTime((Date) datePickerBegin.getModel().getValue())
                    .formatted(BEGIN_SECONDS);
            String selectedDateEnd = convertDateToDateTime((Date) datePickerEnd.getModel().getValue())
                    .formatted(END_SECONDS);

            InputStream inputStream = ImportGasu.class.getClassLoader().getResourceAsStream(inputSqlQuery);

            String query = getQuery(inputStream).formatted(PERIOD_FROM_DATE.formatted(selectedDateBegin),
                    PERIOD_FROM_DATE.formatted(selectedDateEnd));

            buildExelFileFromData(query);

            log.info("Конец извлечения заявлений из ГАСУв файл с программой в каталог output в файл " + outputFileName);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private String getQuery(InputStream inputStream) throws IOException {
        IOUtils.copy(inputStream, writer, "UTF-8");
        return writer.toString();
    }

    private void buildExelFileFromData(String query) throws Exception {
        Class.forName(driverClassName);
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement sta = conn.createStatement();
        ResultSet resultSet = sta.executeQuery(query);
        JdbcAdapter jdbcAdapter = new JdbcAdapter();
        jdbcAdapter.fillDataTable(dataTable, resultSet);
        sheet.insertDataTable(dataTable, true, 1, 1);
        sheet.getAllocatedRange().autoFitColumns();

        workbook.saveToFile("output/%s".formatted(outputFileName), ExcelVersion.Version2016);
    }

    private String convertDateToDateTime(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.toString() + " %s";
    }
}
