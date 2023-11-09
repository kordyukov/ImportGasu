package ru.fors.itconsulting.importgasu.utils;

import lombok.Data;

import javax.swing.*;
import java.awt.*;

@Data
public class Console {
    private JTextArea console = new JTextArea();
    private JScrollPane sp = new JScrollPane(console);

    public Console() {
        console.setFont(new Font("Consolas", Font.PLAIN, 15));
        console.setBounds(0, 0, 800, 500);
        console.setBackground(Color.BLACK);
        console.setForeground(Color.GREEN);
    }
}
