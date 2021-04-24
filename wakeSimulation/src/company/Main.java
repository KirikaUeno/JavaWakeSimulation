package company;
import objects.Particle;
import org.opensourcephysics.numerics.FFT;
import ui.MainFrame;
import ui.PartSimulationMainPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}