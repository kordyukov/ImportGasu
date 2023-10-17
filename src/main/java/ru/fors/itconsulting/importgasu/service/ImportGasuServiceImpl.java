package ru.fors.itconsulting.importgasu.service;

import com.github.jknack.handlebars.internal.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.fors.itconsulting.importgasu.model.LicenseApplications;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportGasuServiceImpl implements ImportGasuService {
    private final JdbcTemplate jdbcTemplate;
    @Override
    public List<LicenseApplications> getLicenseApplications() throws IOException {
        String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                .getFile(), StandardCharsets.UTF_8);

        log.info("start import license applications... from query {}", query);

        List<LicenseApplications> licenseApplications = jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(LicenseApplications.class));

        log.info("end import license applications");

        return licenseApplications;
    }
}
