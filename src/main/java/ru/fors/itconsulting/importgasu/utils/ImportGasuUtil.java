package ru.fors.itconsulting.importgasu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fors.itconsulting.importgasu.constant.Decision;
import ru.fors.itconsulting.importgasu.service.ImportGasuImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static java.util.Collections.emptyMap;


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

    public static Map<String, String> getDecisionMap() {
        try (InputStream inputStreamFromDecision = ImportGasuImpl.class.getClassLoader().getResourceAsStream("decision.yaml")) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String paramFile = getQueryFromIs(inputStreamFromDecision);
            Decision decisionParameters = mapper.readValue(paramFile, Decision.class);

            return decisionParameters.getDecision();
        } catch (IOException ignored) {

        }
        return emptyMap();
    }
}