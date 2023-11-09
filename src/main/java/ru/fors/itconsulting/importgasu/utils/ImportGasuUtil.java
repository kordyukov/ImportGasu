package ru.fors.itconsulting.importgasu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fors.itconsulting.importgasu.maps.Applications;
import ru.fors.itconsulting.importgasu.maps.Decision;
import ru.fors.itconsulting.importgasu.maps.OrganizationCode;
import ru.fors.itconsulting.importgasu.service.ImportGasuImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static java.util.Collections.emptyMap;
import static ru.fors.itconsulting.importgasu.constant.ImportGasuConstant.CONSOLE_TEXT;


public class ImportGasuUtil {
    private static final Logger log = LoggerFactory.getLogger(ImportGasuUtil.class);

    public static String getQueryFromIs(InputStream inputStream) {
        try (StringWriter writer = new StringWriter()) {
            IOUtils.copy(inputStream, writer, "UTF-8");
            return writer.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return EMPTY_STRING;
    }

    public static Map<String, String> getDecisionMap(String fileName) {
        try (InputStream inputStreamFromDecision = ImportGasuImpl.class.getClassLoader().getResourceAsStream(fileName)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String paramFile = getQueryFromIs(inputStreamFromDecision);
            Decision decisionParameters = mapper.readValue(paramFile, Decision.class);

            return decisionParameters.getDecision();
        } catch (IOException ignored) {

        }
        return emptyMap();
    }

    public static Map<String, String> getOrganizationsMap(String fileName) {
        try (InputStream inputStreamFromOrganization = ImportGasuImpl.class.getClassLoader().getResourceAsStream(fileName)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String paramFile = getQueryFromIs(inputStreamFromOrganization);
            OrganizationCode decisionParameters = mapper.readValue(paramFile, OrganizationCode.class);

            return decisionParameters.getOrganizationCode();
        } catch (IOException ignored) {

        }
        return emptyMap();
    }

    public static Map<String, String> getApplicationsMap(String fileName) {
        try (InputStream inputStreamFromApplication = ImportGasuImpl.class.getClassLoader().getResourceAsStream(fileName)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String paramFile = getQueryFromIs(inputStreamFromApplication);
            Applications applicationParameters = mapper.readValue(paramFile, Applications.class);

            return applicationParameters.getApplications();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return emptyMap();
    }

    public static String addTextInConsole(String text) {
        CONSOLE_TEXT += "\n" + new Date() + " " + text;
        return CONSOLE_TEXT;
    }
}
