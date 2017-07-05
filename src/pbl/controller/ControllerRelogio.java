/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.controller;

import pbl.view.Principal;
import pbl.model.IControllerRelogio;

/**
 *
 * @author marcos
 */
public class ControllerRelogio implements IControllerRelogio{

    private final Principal telaPrincipal;
    private final ControllerConexao conexao;
    private boolean coordenador;
    private float drift;
    private int id;
    
    public ControllerRelogio(Principal tela) {
        this.telaPrincipal = tela;
        this.conexao = new ControllerConexao(this);
        this.drift = 1;
    }

    @Override
    public void novaMensagemRecebida(String[] str) {

    }

    @Override
    public long getHorario() {
        return telaPrincipal.getHorario();
    }

    @Override
    public void setHorario(long horario) {
        telaPrincipal.setHorarioSeg(horario);
    }

    @Override
    public void setCoordenador(boolean b) {
        this.coordenador = b;
        this.telaPrincipal.atualizar();
    }

    public float getDrift() {
        return drift;
    }

    public void setDrift(float drift) {
        this.drift = drift;
    }

    public int getID() {
        return this.id;
    }
    
    @Override
    public void setID(int id){
        this.id = id;
//        telaPrincipal.atualizar();
    }

    public boolean isCoordenador(){
        return coordenador;
    }
    
    
}
