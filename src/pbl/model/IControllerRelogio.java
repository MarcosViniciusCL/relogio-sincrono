/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.model;

/**
 *
 * @author marcos
 */
public interface IControllerRelogio {
    
    public void novaMensagemRecebida(String[] str);

    public void forcaAtualizacaoHorario(long horario);
    
    public long getHorario();

    public void setHorario(long parseLong);

    public void setCoordenador(boolean b);

    public void setID(int id);
}
