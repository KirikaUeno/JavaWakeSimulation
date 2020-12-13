package com.ui;

import com.company.Constants;
import com.image.*;
import com.image.Image;

import javax.swing.*;
import java.util.Objects;

public class MainFrame extends JFrame {
    private PartSimulationMainPanel partSimulationMainPanel;
    private CirculantsMainPanel circulantsMainPanel;
    public MainFrame() {
        initializeLayout();
    }

    private void initializeLayout() {
        setTitle(Constants.title);
        setIconImage(Objects.requireNonNull(ImageFactory.createImage(Image.ICON)).getImage());

        partSimulationMainPanel=new PartSimulationMainPanel(this);
        add(partSimulationMainPanel);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        pack();
    }
    /**
     * add circulants and remove partSimulation from mainPanel, but doesnt stop its processes. Be shure to stop all of them before
     * calling this function.
     */
    public void swapToCirculants(){
        remove(partSimulationMainPanel);
        circulantsMainPanel=new CirculantsMainPanel(this);
        add(circulantsMainPanel);
        revalidate();
    }
    /**
     * add partSimulation and remove circulants from mainPanel, but doesnt stop its processes. Be shure to stop all of them before
     * calling this function.
     */
    public void swapToSimulation(){
        remove(circulantsMainPanel);
        partSimulationMainPanel=new PartSimulationMainPanel(this);
        add(partSimulationMainPanel);
        revalidate();
    }
}
