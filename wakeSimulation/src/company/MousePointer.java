package company;

import ui.GraphPanel;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class MousePointer extends MouseInputAdapter {
    private final GraphPanel panel;

    public MousePointer(GraphPanel panel){
        this.panel = panel;
    }

    public void mouseClicked(MouseEvent e) {
        int xClicked = e.getX();
        this.panel.drawLine = true;
        this.panel.xLine = xClicked;
        this.panel.repaint();
    }
}
