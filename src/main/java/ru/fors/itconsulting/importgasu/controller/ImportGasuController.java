package ru.fors.itconsulting.importgasu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fors.itconsulting.importgasu.model.LicenseApplications;
import ru.fors.itconsulting.importgasu.service.ImportGasuService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class ImportGasuController {
    private final ImportGasuService importGasuService;
    @GetMapping("import")
    public List<LicenseApplications> getLicenseApplications() throws IOException, ExecutionException, InterruptedException {
        return importGasuService.getLicenseApplications();
    }
}
