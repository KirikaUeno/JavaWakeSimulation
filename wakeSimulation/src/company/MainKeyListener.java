package company;

import ui.PartSimulationMainPanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainKeyListener extends KeyAdapter {
    private final PartSimulationMainPanel board;

    public MainKeyListener(PartSimulationMainPanel board) {
        this.board = board;
    }


    public void keyReleased(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_SPACE) board.doOneLoop();
    }
}
