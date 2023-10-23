package ru.fors.itconsulting.importgasu.config;

import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static ru.fors.itconsulting.importgasu.constant.ImportGasuConstant.DATE_PATTERN;

public class DateLabelFormatter extends AbstractFormatter {
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }

        return EMPTY_STRING;
    }

}
