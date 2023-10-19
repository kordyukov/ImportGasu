package ru.fors.itconsulting.importgasu.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.fors.itconsulting.importgasu.service.ImportGasuService;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequiredArgsConstructor
public class ImportGasuController {
    private final ImportGasuService importGasuService;

    @GetMapping(
            value = "/import",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> getFile() throws IOException {
        importGasuService.saveResultFileApplications();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Applications.xlsx");

        InputStream in = getClass()
                .getResourceAsStream("/ExportToExcel.xlsx");

        return new ResponseEntity<>(IOUtils.toByteArray(in), header, HttpStatus.OK);
    }
}
