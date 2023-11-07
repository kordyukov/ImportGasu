package ru.fors.itconsulting.importgasu.service;

import com.spire.data.table.DataTable;
import com.spire.data.table.common.JdbcAdapter;
import com.spire.xls.ExcelVersion;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.fors.itconsulting.importgasu.config.DateLabelFormatter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static ru.fors.itconsulting.importgasu.constant.ImportGasuConstant.*;


@Component
public class ImportGasuImpl extends JFrame implements CommandLineRunner, ImportGasu {
    private static final Logger log = LoggerFactory.getLogger(ImportGasuImpl.class);
    private final Properties properties;

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
    @Value("${files.input-sql-query-application}")
    private String inputSqlQueryFromApplication;

    public ImportGasuImpl(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... arg0) {
        JDatePickerImpl datePickerBegin = getJDatePickerImpl();
        JDatePickerImpl datePickerEnd = getJDatePickerImpl();

        EventQueue.invokeLater(() -> {
            log.info("Старт загрузки приложения по выгрузке данных с {}", url);

            JPanel databaseAddres = new JPanel();
            JLabel urlText = new JLabel("Адрес базы: " + url);
            databaseAddres.add(urlText);

            JButton importButton = new JButton("Выгрузить");
            importButton.addActionListener((ActionEvent event) -> importFromDataBase(datePickerBegin, datePickerEnd));

            JButton quitButton = new JButton("Закрыть");
            quitButton.addActionListener((ActionEvent event) -> System.exit(0));

            JPanel panel = initPanel(importButton,
                    quitButton, datePickerBegin, datePickerEnd);

            JFrame window = initWindow();

            window.add(databaseAddres, BorderLayout.LINE_START);
            window.add(panel, BorderLayout.AFTER_LAST_LINE);

            log.info("Конец загрузки приложения по выгрузке данных с {}", url);
        });
    }

    private JPanel initPanel(JButton importButton,
                             JButton quitButton,
                             JDatePickerImpl datePickerBegin,
                             JDatePickerImpl datePickerEnd) {
        JPanel panel = new JPanel();
        panel.add(importButton);
        panel.add(new JLabel("Выберите периоды: "));
        panel.add(datePickerBegin);
        panel.add(datePickerEnd);
        panel.add(quitButton);

        return panel;
    }

    private JFrame initWindow() {
        JFrame window = new JFrame("ImportGasu приложение для импорта, база: " + url);
        window.setBounds(50, 50, 1000, 100);
        window.setVisible(true);
        window.setResizable(false);
        window.setBackground(Color.GREEN);
        return window;
    }

    private JDatePickerImpl getJDatePickerImpl() {
        UtilDateModel model = new UtilDateModel();
        model.setValue(DateUtils.addDays(new Date(),-1));
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.setBackground(Color.GREEN);
        datePicker.setBorder(new BevelBorder(BevelBorder.LOWERED));

        return datePicker;
    }

    @Override
    public void importFromDataBase(JDatePickerImpl datePickerBegin, JDatePickerImpl datePickerEnd) {
        try (InputStream inputStreamFromApplication = ImportGasuImpl.class.getClassLoader().getResourceAsStream(inputSqlQueryFromApplication)) {
            String selectedDateBegin = convertDateToDateTime((Date) datePickerBegin.getModel().getValue())
                    .formatted(BEGIN_SECONDS);
            String selectedDateEnd = convertDateToDateTime((Date) datePickerEnd.getModel().getValue())
                    .formatted(END_SECONDS);

            String query = getQuery(inputStreamFromApplication).formatted(PERIOD_FROM_DATE.formatted(selectedDateBegin),
                    PERIOD_FROM_DATE.formatted(selectedDateEnd));

            log.info(START_MESSAGE.formatted(url, selectedDateBegin, selectedDateEnd));

            buildExelFileFromData(query);
            JOptionPane.showMessageDialog(null, COMPLETE_MESSAGE + outputFileName);

            openFile();

            log.info(COMPLETE_MESSAGE + outputFileName);
        } catch (Exception e) {
            log.error(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage());

        }

    }

    private String getQuery(InputStream inputStream) {
        try (StringWriter writer = new StringWriter()) {
            IOUtils.copy(inputStream, writer, "UTF-8");
            return writer.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return EMPTY_STRING;
    }

    private void buildExelFileFromData(String query) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement sta = conn.createStatement();
             ResultSet resultSet = sta.executeQuery(query)) {
            checkAndDeleteFile();
            Class.forName(driverClassName);

            DataTable dataTable = new DataTable();

            JdbcAdapter jdbcAdapter = new JdbcAdapter();
            jdbcAdapter.fillDataTable(dataTable, resultSet);

            Workbook workbook = new Workbook();
            Worksheet sheet = workbook.getWorksheets().get(0);

            sheet.insertDataTable(dataTable, true, 1, 1);
            sheet.getAllocatedRange().autoFitColumns();
            sheet.setName("Данные о заявлениях, решения.");

            buildGuideFromParameters(sheet, workbook);

            workbook.getWorksheets().get(2).remove();

            workbook.saveToFile(outputFileName, ExcelVersion.Version2016);
        }

    }

    private void buildGuideFromParameters(Worksheet sheet, Workbook workbook) {
        int length = sheet.getRows().length;
        ArrayList<String> resultFromGuideList = buildListParametersFromGuide(1, 13, length, 2, sheet);
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 2, length, 1, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 12, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 19, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 17, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 20, length, 2, sheet));

        Worksheet dopSheet = workbook.getWorksheets().get(1);
        dopSheet.setName("Лист1");
        dopSheet.insertArrayList(resultFromGuideList, 1, 1, true, true);
    }

    private ArrayList<String> buildListParametersFromGuide(int rowNumber,
                                                           int columnNumber,
                                                           int length,
                                                           int step,
                                                           Worksheet sheet) {
        ArrayList<String> result = IntStream.range(rowNumber, length)
                .mapToObj(i -> sheet.get(i, columnNumber)
                        .getValue()).filter(value -> !EMPTY_STRING.equals(value)).distinct().collect(Collectors.toCollection(ArrayList::new));
        IntStream.range(0, step).forEach(i -> result.add(EMPTY_STRING));
        return result;
    }

    private String convertDateToDateTime(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.toString() + " %s";
    }

    private void openFile() throws IOException {
        File file = new File(outputFileName);

        if (!Desktop.isDesktopSupported()) {
            log.error(DESKTOP_IS_NO_SUPPORTED);
            JOptionPane.showMessageDialog(null, DESKTOP_IS_NO_SUPPORTED);

            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) desktop.open(file);
    }

    private void checkAndDeleteFile() {
        File fileToDelete = new File(outputFileName);
        if (fileToDelete.exists()) {
            boolean success = fileToDelete.delete();

            if (!success) {
                log.info("Закройте отчет для загрузки нового!");
                JOptionPane.showMessageDialog(null, "Закройте отчет для загрузки нового!");
            }
        } else {
            log.info("Отчет отсутствуев в {}, загружаем...", outputFileName);
        }
    }
}
