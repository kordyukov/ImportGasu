package ru.fors.itconsulting.importgasu.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LicenseApplications {
    private String inputApplicationNumber;
    private String applicationType;
    private String permissiveModeId;
    private String modeName;
    private String typeErulCode;
    private String erulName;
    private LocalDate registrationDate;
    private String permittingAuthorityId;
    private String licensingAuthority;
    private String subjectCode;
    private String subjectName;
    private String referralMethod;
    private String contragentType;
    private String applicantName;
    private String ogrn;
    private String inn;
    private String decision;
    private String reasonRefusal;
    private LocalDate dateDecision;
    private String numberDecision;
    private LocalDate dateGrantingPermission;
    private LocalDate dateTerminationsPermission;
    private String statusDecision;
}
