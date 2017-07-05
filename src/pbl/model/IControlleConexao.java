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
public interface IControlleConexao {
    //******************************************** ENVIO DE MENSAGEM ************************************** 
    public void atualizarHorario(long horario);
    
    public void iniciarEleicao();
    
    public void enviarHorarioEleicao();
 
    public void novoRelogio();
 
    public void iniciarTesteDelay();
    
    public void solicitarID();

    //********************************** RECEBIMENTO MENSAGEM **********************************************
    public void atualizarHorarioR(String[] str);
    
    public void iniciarEleicaoR(String[] str);
    
    public void enviarHorarioEleicaoR(String[] str);
    
    public void novoRelogioR(String[] str);
    
    public void iniciarTesteDelayR();
    
    public void idPemitido(String[] str);

    //******************************************************************************************************

    public void mensagemDesconhecida(String[] str);
}
