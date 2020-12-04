package com.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UiLoop implements ActionListener {

    private final PartSimulationMainPanel partSimulationMainPanel;

    public UiLoop(PartSimulationMainPanel partSimulationMainPanel) {
        this.partSimulationMainPanel = partSimulationMainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.partSimulationMainPanel.doOneLoop();
    }
}
