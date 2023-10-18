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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportGasuServiceImpl implements ImportGasuService {
    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService executor
            = Executors.newSingleThreadExecutor();
    @Override
    public List<LicenseApplications> getLicenseApplications() throws IOException, InterruptedException, ExecutionException {
        List<LicenseApplications> result = new ArrayList<>();
        int limit = 2000;
        String query = Files.read(new ClassPathResource("/applicationJdbc.sql")
                .getFile(), StandardCharsets.UTF_8);

        log.info("start import license applications... from query {}", query);
        Integer count = jdbcTemplate.queryForObject("select count(id) from license.application", Integer.class);

        log.info("end import license applications");
        List<LicenseApplications> licenseApplications;
        for (int i = 0; i < count; i = i + limit) {
            licenseApplications = asyncGetApplications(query.formatted(i + limit, i + limit - 1), result, count);
            result.addAll(licenseApplications);
        }

        return result;
    }

    private List<LicenseApplications> asyncGetApplications(String query, List<LicenseApplications> result, int count) throws InterruptedException, ExecutionException {
        Future<List<LicenseApplications>> applications = getApplications(query);

        while (!applications.isDone()) {
            log.info("Getting rows... {} from {} rows", result.size(), count);

            Thread.sleep(1000);
        }

        return applications.get();
    }

    public Future<List<LicenseApplications>> getApplications(String query) {
        return executor.submit(() -> jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(LicenseApplications.class)));
    }
}
