/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author marcos
 */
public class BotaoAction extends AbstractAction {

    private int tecla;
    private Principal frame;

    public BotaoAction(int tecla, Principal frame) {
        this.tecla = tecla;
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tecla == 'd') {
            new TelaDrift(frame).setVisible(true);
        }
        if (tecla == 'h') {
            new TelaHorario(frame).setVisible(true);
        }
    }

}
