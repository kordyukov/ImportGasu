package ru.fors.itconsulting.importgasu.service;

import com.github.jknack.handlebars.internal.lang3.ArrayUtils;
import com.spire.data.table.DataTable;
import com.spire.data.table.common.JdbcAdapter;
import com.spire.xls.ExcelVersion;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
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
import ru.fors.itconsulting.importgasu.utils.Console;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static ru.fors.itconsulting.importgasu.constant.ImportGasuConstant.*;
import static ru.fors.itconsulting.importgasu.utils.ImportGasuUtil.*;


@Component
public class ImportGasuImpl extends JFrame implements CommandLineRunner, ImportGasu {
    private static final Logger log = LoggerFactory.getLogger(ImportGasuImpl.class);
    private final Properties properties;
    private final Console console;

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
    @Value("${files.decision-map}")
    private String decisionFileName;
    @Value("${files.organization-code}")
    private String organizationCodeFileName;
    @Value("${files.application-guide}")
    private String applicationGuideFileName;

    public ImportGasuImpl(Properties properties, Console console) {
        this.properties = properties;
        this.console = console;
    }


    @Override
    public void run(String... arg0) {
        JDatePickerImpl datePickerBegin = getJDatePickerImpl();
        JDatePickerImpl datePickerEnd = getJDatePickerImpl();

        EventQueue.invokeLater(() -> {
            log.info("Старт загрузки приложения по выгрузке данных с {}", url);
            appendAndUpdateConsole(CONSOLE_TEXT);
            appendAndUpdateConsole("Старт загрузки приложения по выгрузке данных с " + url);

            JPanel databaseAddres = new JPanel();
            JLabel urlText = new JLabel("Адрес базы: " + url);
            databaseAddres.add(urlText);

            JButton importButton = new JButton("Выгрузить");

            JFrame window = initWindow();

            importButton.addActionListener((ActionEvent event) -> importFromDataBase(datePickerBegin, datePickerEnd));

            JButton quitButton = new JButton("Закрыть");
            quitButton.addActionListener((ActionEvent event) -> System.exit(0));

            JPanel panel = initPanel(importButton,
                    quitButton, datePickerBegin, datePickerEnd);

            window.getContentPane().add(console.getSp(), BorderLayout.CENTER);
            window.add(panel, BorderLayout.AFTER_LAST_LINE);
            log.info("Конец загрузки приложения по выгрузке данных с {}", url);
            appendAndUpdateConsole("Конец загрузки приложения по выгрузке данных с " + url);
        });
    }

    private void appendAndUpdateConsole(String text) {
        JTextArea jTextArea = console.getConsole();
        jTextArea.append(addTextInConsole(text));
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
        JFrame window = new JFrame("ImportGasu приложение для загрузки отчета по лицензиям, база: " + url);
        window.setBounds(50, 50, 800, 300);
        window.setVisible(true);
        window.setResizable(true);
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
    public void importFromDataBase(JDatePickerImpl datePickerBegin,
                                   JDatePickerImpl datePickerEnd) {
        try (InputStream inputStreamFromApplication = ImportGasuImpl.class.getClassLoader().getResourceAsStream(inputSqlQueryFromApplication)) {
            String selectedDateBegin = convertDateToDateTime((Date) datePickerBegin.getModel().getValue())
                    .formatted(BEGIN_SECONDS);
            String selectedDateEnd = convertDateToDateTime((Date) datePickerEnd.getModel().getValue())
                    .formatted(END_SECONDS);

            String query = getQueryFromIs(inputStreamFromApplication).formatted(PERIOD_FROM_DATE.formatted(selectedDateBegin),
                    PERIOD_FROM_DATE.formatted(selectedDateEnd));

            log.info(START_MESSAGE.formatted(url, selectedDateBegin, selectedDateEnd));
            appendAndUpdateConsole(START_MESSAGE.formatted(url, selectedDateBegin, selectedDateEnd));
            updateFrameFromLog(console.getConsole());

            buildExelFileFromData(query);
            JOptionPane.showMessageDialog(null, COMPLETE_MESSAGE + outputFileName);

            openFile();

            log.info(COMPLETE_MESSAGE + outputFileName);
            appendAndUpdateConsole(COMPLETE_MESSAGE + outputFileName);
            updateFrameFromLog(console.getConsole());
        } catch (Exception e) {
            log.error(e.getMessage());
            appendAndUpdateConsole(e.getMessage());
            updateFrameFromLog(console.getConsole());
            JOptionPane.showMessageDialog(null, e.getMessage());

        }

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

    private void updateFrameFromLog(JTextArea window) {
        window.update(window.getGraphics());
    }

    private void buildGuideFromParameters(Worksheet sheet, Workbook workbook) {
        editDecisionsType(sheet);
//TODO ставим по дефолту пока 00109
//      editCodeFromOrganizations(sheet);
        editApplicationType(sheet);
        buildFromGuideList(sheet, workbook.getWorksheets().get(1));
    }

    private void buildFromGuideList(Worksheet sheet, Worksheet dopSheet) {
        int length = sheet.getRows().length;
        ArrayList<String> resultFromGuideList = buildListParametersFromGuide(1, 13, length, 2, sheet);
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 2, length, 1, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 12, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 19, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 17, length, 2, sheet));
        resultFromGuideList.addAll(buildListParametersFromGuide(2, 20, length, 2, sheet));

        dopSheet.setName("Лист1");

        dopSheet.insertArrayList(resultFromGuideList, 1, 1, true, true);
    }

    private void editCodeFromOrganizations(Worksheet sheet) {
        log.info(START_UPDATE_TYPE_ORG);
        appendAndUpdateConsole(START_UPDATE_TYPE_ORG);
        updateFrameFromLog(console.getConsole());
        Map<String, String> organizationsMap = getOrganizationsMap(organizationCodeFileName);
        int length = sheet.getRows().length;
        IntStream.range(2, length)
                .forEach(i -> {
                    String value = sheet.get(i, 9).getValue();
                    if (value != null) {
                        if (organizationsMap.containsKey(value.toLowerCase())) {
                            sheet.get(i, 8).setValue(organizationsMap.get(value.toLowerCase()));
                        }
                    }
                });
        log.info(END_UPDATE_TYPE_ORG);
        appendAndUpdateConsole(END_UPDATE_TYPE_ORG);
        updateFrameFromLog(console.getConsole());
    }

    private void editApplicationType(Worksheet sheet) {
        log.info(START_UPDATE_TYPE_APP);
        appendAndUpdateConsole(START_UPDATE_TYPE_APP);
        updateFrameFromLog(console.getConsole());
        Map<String, String> applicationsMap = getApplicationsMap(applicationGuideFileName);
        int length = sheet.getRows().length + 1;

        List<Integer> ids = new ArrayList<>();

        IntStream.range(2, length).forEachOrdered(i -> {
            String value = sheet.get(i, 2).getValue();
            log.info("Тип заявления: {}", value);
            appendAndUpdateConsole("Тип заявления: " + value);
            updateFrameFromLog(console.getConsole());
            if (value != null) {
                String cleanValue = value.trim();

                if (applicationsMap.containsKey(cleanValue)) {
                    sheet.get(i, 2).setValue(applicationsMap.get(cleanValue));
                    log.info("Обновление типа заявления {}", applicationsMap.get(cleanValue));
                    appendAndUpdateConsole("Обновление типа заявления " + applicationsMap.get(cleanValue));
                    updateFrameFromLog(console.getConsole());
                } else {
                    ids.add(i);
                    log.info("Сохранение индекса неактуального типа заявления: {}, row: {}", value, i);
                    appendAndUpdateConsole("Сохранение индекса неактуального типа заявления: %s, строка: %s".formatted(value, i));
                    updateFrameFromLog(console.getConsole());
                }
            }
        });

        sheet.deleteRows(ArrayUtils.toPrimitive(ids.toArray(new Integer[0])));

        log.info(END_UPDATE_TYPE_APP);
        appendAndUpdateConsole(END_UPDATE_TYPE_APP);
        updateFrameFromLog(console.getConsole());
    }

    private void editDecisionsType(Worksheet sheet) {
        log.info(START_UPDATE_TYPE_DECISION);
        appendAndUpdateConsole(START_UPDATE_TYPE_DECISION);
        updateFrameFromLog(console.getConsole());

        Map<String, String> decisionMap = getDecisionMap(decisionFileName);
        int length = sheet.getRows().length;
        IntStream.range(2, length)
                .forEach(i -> {
                    String value = decisionMap.get(sheet.get(i, 19).getValue());
                    log.info("Тип решения %s".formatted(value));
                    appendAndUpdateConsole("Тип решения %s".formatted(value));
                    updateFrameFromLog(console.getConsole());
                    if (value != null) {
                        sheet.get(i, 19).setValue(value);
                        log.info("Обновления типа решения %s".formatted(value));
                        appendAndUpdateConsole("Обновления типа решения %s".formatted(value));
                        updateFrameFromLog(console.getConsole());
                    }
                });
        log.info(END_UPDATE_TYPE_DECISION);
        appendAndUpdateConsole(END_UPDATE_TYPE_DECISION);
        updateFrameFromLog(console.getConsole());
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
            appendAndUpdateConsole(DESKTOP_IS_NO_SUPPORTED);
            updateFrameFromLog(console.getConsole());
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
                log.info(CLOSE_REPORT_MESSAGE);
                appendAndUpdateConsole(CLOSE_REPORT_MESSAGE);
                updateFrameFromLog(console.getConsole());
                JOptionPane.showMessageDialog(null, CLOSE_REPORT_MESSAGE);
            }
        } else {
            log.info("Отчет отсутствует в {}, загружаем...", outputFileName);
            appendAndUpdateConsole("Отчет отсутствует в %s, загружаем...".formatted(outputFileName));
            updateFrameFromLog(console.getConsole());
        }
    }
}
