package com.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UiLoop implements ActionListener {

    private final MainPanel mainPanel;

    public UiLoop(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.mainPanel.doOneLoop();
    }
}
