package ru.fors.itconsulting.importgasu.service;

import org.jdatepicker.impl.JDatePickerImpl;

public interface ImportGasu {
    void importFromDataBase(JDatePickerImpl datePickerBegin, JDatePickerImpl datePickerEnd);
}
