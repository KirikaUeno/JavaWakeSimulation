package ui;

import company.Constants;

import javax.swing.*;

public class MainFrame extends JFrame {
    private PartSimulationMainPanel partSimulationMainPanel;
    private CirculantsMainPanel circulantsMainPanel;
    public MainFrame() {
        initializeLayout();
    }

    private void initializeLayout() {
        setTitle(Constants.title);
        ImageIcon icon = new ImageIcon("images/appIcon.jpg");
        setIconImage(icon.getImage());

        partSimulationMainPanel = new PartSimulationMainPanel(this);
        add(partSimulationMainPanel);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
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
