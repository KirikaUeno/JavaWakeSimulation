package com.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UILoop1 implements ActionListener {

    private final CirculantsMainPanel circulaantsMainPanel;

    public UILoop1(CirculantsMainPanel circulaantsMainPanel) {
        this.circulaantsMainPanel = circulaantsMainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.circulaantsMainPanel.doOneLoop();
    }
}
