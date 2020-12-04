package com.ui;

import com.company.Constants;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private PartSimulationMainPanel partSimulationMainPanel;
    private CirculantsMainPanel circulantsMainPanel;

    public MainPanel(){
        setPreferredSize(new Dimension(Constants.boardWight, Constants.boardHeight));

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        partSimulationMainPanel=new PartSimulationMainPanel(this);

        add(partSimulationMainPanel);
    }

    public void swapToCirculants(){
        remove(partSimulationMainPanel);
        circulantsMainPanel=new CirculantsMainPanel(this);
        add(circulantsMainPanel);
        revalidate();
    }

    public void swapToSimulation(){
        remove(circulantsMainPanel);
        partSimulationMainPanel=new PartSimulationMainPanel(this);
        add(partSimulationMainPanel);
        revalidate();
    }
}
