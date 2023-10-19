package ru.fors.itconsulting.importgasu.service;

import com.github.jknack.handlebars.internal.Files;
import com.spire.data.table.DataTable;
import com.spire.data.table.common.JdbcAdapter;
import com.spire.xls.ExcelVersion;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.fors.itconsulting.importgasu.model.LicenseApplications;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportGasuServiceImpl implements ImportGasuService {
    private final JdbcTemplate jdbcTemplate;
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

    @Override
    public List<LicenseApplications> getLicenseApplications() throws IOException {
        List<LicenseApplications> result = new ArrayList<>();
        String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                .getFile(), StandardCharsets.UTF_8);

        log.info("start import license applications... ");

        var count = jdbcTemplate.queryForObject("select count(id) from license.application", Integer.class);
        List<LicenseApplications> licenseApplications = null;

        for (int i = 0; i < count; i = i + limit) {
            if (i > count) {
                int endLimit = count - i;
                log.info("i = {}", i);
                licenseApplications = getApplications(query.formatted(licenseApplications.size() - 1, count - endLimit), result, count);
                log.info("Total entries : {}, remained : {}" , licenseApplications.size() - 1, count - endLimit);
                result.addAll(licenseApplications);
            }
            licenseApplications = getApplications(query.formatted(limit, i == 0 ? 0 : i - 1), result, count);
            result.addAll(licenseApplications);
        }

        log.info("end import license applications");

        return result;
    }

    @Override
    public void saveResultFileApplications() throws IOException {
        //Create a Workbook object
        Workbook wb = new Workbook();

        //Get the first worksheet
        Worksheet sheet = wb.getWorksheets().get(0);

        //Create a DataTable object
        DataTable dataTable = new DataTable();

        String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                .getFile(), StandardCharsets.UTF_8);

        try {
            Class.forName(driverClassName);
            try {
                Connection conn = DriverManager.getConnection(url, username, password);
                Statement sta = conn.createStatement();

                //Select table from the database
                ResultSet resultSet = sta.executeQuery(query);
                JdbcAdapter jdbcAdapter = new JdbcAdapter();

                //Export data from database to datatable
                jdbcAdapter.fillDataTable(dataTable, resultSet);

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Write datatable to the worksheet
        sheet.insertDataTable(dataTable, true, 1, 1);

        //Auto fit column width
        sheet.getAllocatedRange().autoFitColumns();

        //Save to an Excel file
        wb.saveToFile("src/main/resources/ExportToExcel.xlsx", ExcelVersion.Version2016);
    }

    private List<LicenseApplications> getApplications(String query, List<LicenseApplications> result, int count) {
        log.info("Getting rows... {} from {} rows", result.size(), count);

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(LicenseApplications.class));
    }
}
