package ru.fors.itconsulting.importgasu.service;

import org.jdatepicker.impl.JDatePickerImpl;

import javax.swing.*;

public interface ImportGasu {
    void importFromDataBase(JDatePickerImpl datePickerBegin, JDatePickerImpl datePickerEnd, JLabel status);
}
