package ru.fors.itconsulting.importgasu.config;

import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

public class DateLabelFormatter extends AbstractFormatter {

    private final String datePattern = "dd.MM.yyyy";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

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