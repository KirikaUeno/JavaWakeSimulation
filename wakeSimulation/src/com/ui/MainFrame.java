package com.ui;

import com.company.Constants;
import com.image.*;
import com.image.Image;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        initializeLayout();
    }

    private void initializeLayout() {
        setTitle(Constants.title);
        setIconImage(ImageFactory.createImage(Image.ICON).getImage());

        add(new MainPanel());
        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
