/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.controller;

import pbl.view.Principal;
import pbl.model.IControllerRelogio;

/**
 * Classe responsável por gerenciar todo o relógio.
 * @author marcos
 */
public class ControllerRelogio implements IControllerRelogio{

    private final Principal telaPrincipal;
    private final ControllerConexao controllerConexao;
    ControllerRelogio controller;
    private boolean coordenador;
    private float drift;
    private int id;
    
    public ControllerRelogio(Principal tela) {
        this.telaPrincipal = tela;
        this.controllerConexao = new ControllerConexao(this);
        this.drift = 1;
    }

    @Override
    public void novaMensagemRecebida(String[] str) {
    }

    /**
     * Retona o horário do relógio.
     * @return long - horario em segundos
     */
    @Override
    public long getHorario() {
        return telaPrincipal.getHorario();
    }

    /**
     * Adiciona o horário recebido como parâmetro no relógio.
     * @param horario - tempo em segundos.
     */
    @Override
    public void setHorario(long horario) {
        telaPrincipal.setHorarioSeg(horario);
    }

    /**
     * Adiciona se é coordenador.
     * @param b - boolean
     */
    @Override
    public void setCoordenador(boolean b) {
        this.coordenador = b;
        this.telaPrincipal.atualizar();
    }

    /**
     * Retorna o drift do relogio no momento.
     * @return float - drift relógio
     */
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

    @Override
    public void forcaAtualizacaoHorario(long horario) {
        if(coordenador){
            controllerConexao.atualizarHorario(horario);
        }
    }
    
    
}
