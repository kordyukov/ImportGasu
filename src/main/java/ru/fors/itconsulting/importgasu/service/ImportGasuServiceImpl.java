package ru.fors.itconsulting.importgasu.service;

import com.github.jknack.handlebars.internal.Files;
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
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportGasuServiceImpl implements ImportGasuService {
    private final JdbcTemplate jdbcTemplate;
    @Value("${limit}")
    private Integer limit;

    @Override
    public List<LicenseApplications> getLicenseApplications() throws IOException {
        List<LicenseApplications> result = new ArrayList<>();
        String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                .getFile(), StandardCharsets.UTF_8);

        log.info("start import license applications... ");

        var count = jdbcTemplate.queryForObject("select count(id) from license.application", Integer.class);

        if (count == null) {
            return emptyList();
        }

        List<LicenseApplications> licenseApplications;

        for (int i = 0; i < count; i = i + limit) {
            licenseApplications = asyncGetApplications(query.formatted(limit, i == 0 ? 0 : i - 1), result, count);
            result.addAll(licenseApplications);
        }

        log.info("end import license applications");

        return result;
    }

    private List<LicenseApplications> asyncGetApplications(String query, List<LicenseApplications> result, int count) {
        log.info("Getting rows... {} from {} rows", result.size(), count);

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(LicenseApplications.class));
    }
}
