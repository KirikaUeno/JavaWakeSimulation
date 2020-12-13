package com.company;

import com.ui.ResizableJPanel;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Able to make rectangle as mouse dragged and calls disableRect when mouse released
 */
public class MainMouseListener extends MouseInputAdapter {
    private final ResizableJPanel panel;
    private int startX1;
    private int startY1;
    private int MODE;

    public MainMouseListener(ResizableJPanel panel){
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.startX1 = e.getX();
        this.startY1 = e.getY();
        if(startX1<10 && startY1<10) MODE=1;
        else MODE=0;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(MODE==0) {
            int currentX = e.getX();
            int currentY = e.getY();
            panel.setRect(startX1, startY1, currentX, currentY);
            e.getComponent().repaint();
        }
        if(MODE==1) {
            e.getComponent().setPreferredSize(new Dimension(e.getComponent().getWidth()-e.getX(), e.getComponent().getHeight()-e.getY()));
            e.getComponent().revalidate();
            e.getComponent().repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e){
        if(MODE==0) {
            panel.disableRect();
        }

    }

    public void mouseClicked(MouseEvent e) {
        e.getComponent().requestFocus();
    }

    public void mouseMoved(MouseEvent e) {
        if(e.getX()<10 && e.getY()<10) e.getComponent().setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
        else e.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
