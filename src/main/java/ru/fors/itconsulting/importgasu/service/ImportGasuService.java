package ru.fors.itconsulting.importgasu.service;

import ru.fors.itconsulting.importgasu.model.LicenseApplications;

import java.io.IOException;
import java.util.List;

public interface ImportGasuService {
    List<LicenseApplications> getLicenseApplications() throws IOException;

}
