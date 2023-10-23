package ru.fors.itconsulting.importgasu.service;

import com.github.jknack.handlebars.internal.Files;
import com.spire.data.table.DataTable;
import com.spire.data.table.common.JdbcAdapter;
import com.spire.xls.ExcelVersion;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.fors.itconsulting.importgasu.config.DateLabelFormatter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportGasu extends JFrame implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;
    private final Properties properties;
    @Value("${limit}")
    private Integer limit;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    private JPanel contentPane;

    @Override
    public void run(String... arg0) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JPanel databaseAddres = new JPanel();
                JLabel urlText = new JLabel("Адрес базы: " + url);
                JLabel userText = new JLabel("Юзер: " + username);

                JDatePickerImpl datePickerBegin = getJDatePickerImpl();
                JDatePickerImpl datePickerEnd = getJDatePickerImpl();


//                JXDatePicker pickerEnd = new JXDatePicker();
//                pickerEnd.setDate(Calendar.getInstance().getTime());
//                pickerEnd.setFormats(new SimpleDateFormat("dd.MM.yyyy"));
//                pickerEnd.setToolTipText("Дата конца периода");
//                JTextField passwordText = new JTextField(EMPTY_STRING);
//                var importButton = new JButton("Импорт");
//                var quitButton = new JButton("Закрыть");

//                importButton.addActionListener((ActionEvent event) -> importFromDataBase());
//
//                quitButton.addActionListener((ActionEvent event) -> {
//                    System.exit(0);
//                });


                JFrame window = new JFrame("ImportGasu приложение для импорта, база: " + url);
                window.setBounds(50, 50, 600, 480);
                window.setVisible(true);


                databaseAddres.add(urlText);
                window.add(userText, BorderLayout.NORTH);
                window.add(databaseAddres, BorderLayout.BEFORE_LINE_BEGINS);

                JPanel panel = new JPanel();
                panel.add(new JLabel("Выберите периоды: "));
                panel.add(datePickerBegin);
                panel.add(datePickerEnd);
                window.add(panel, BorderLayout.AFTER_LAST_LINE);

//                window.add(userText);
//                window.add(passwordText);
//                window.add(quitButton);
//                window.add(importButton);
            }
        });
    }

    private JDatePickerImpl getJDatePickerImpl() {
        UtilDateModel model = new UtilDateModel();
        model.setValue(new Date());
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.setBackground(Color.GREEN);
        datePicker.setBorder(new BevelBorder(1));

        return datePicker;
    }

    public void importFromDataBase() {
        try {
            Workbook wb = new Workbook();
            Worksheet sheet = wb.getWorksheets().get(0);
            DataTable dataTable = new DataTable();

            String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                    .getFile(), StandardCharsets.UTF_8);


            Class.forName(driverClassName);

            Connection conn = DriverManager.getConnection(url, username, password);
            Statement sta = conn.createStatement();

            ResultSet resultSet = sta.executeQuery(query);
            JdbcAdapter jdbcAdapter = new JdbcAdapter();

            jdbcAdapter.fillDataTable(dataTable, resultSet);

            sheet.insertDataTable(dataTable, true, 1, 1);

            sheet.getAllocatedRange().autoFitColumns();

            wb.saveToFile("src/main/resources/ExportToExcel.xlsx", ExcelVersion.Version2016);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
