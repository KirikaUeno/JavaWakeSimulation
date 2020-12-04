package com.company;

import com.ui.PartSimulationMainPanel;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class MainMouseListener extends MouseInputAdapter {
    private final PartSimulationMainPanel board;
    private int startX1;
    private int startY1;
    private int currentX;
    private int currentY;
    private int wasPressed =0;
    public MainMouseListener(PartSimulationMainPanel board){
        this.board = board;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(wasPressed==0) {
            this.startX1 = e.getX();
            this.startY1 = e.getY();
            wasPressed=1;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentX=e.getX();
        currentY=e.getY();
        board.setRect(startX1,startY1,currentX,currentY);
        board.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e){
        wasPressed =0;
        board.disableRect();
    }
}
