/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.controller;

import pbl.model.IControlleConexao;

/**
 * Esta classe determina as ações que serão tomada baseada no protocolo de comunicação.
 * @author marcos
 */
public class Protocolo {
    private final IControlleConexao classe;
    
    //********************************** PROTOCOLOS DE REDE *******************************************************
    private final int protGenerico = Integer.MAX_VALUE;
    private final int protNovoCliente = 0;              //Informa que existe mais um novo cliente no grupo;
    private final int protEleicaoIniciar = 1;           // Iniciar eleição de coordenador;
    private final int protEleicaoHorario = 10;          //Quando a eleição é iniciar todos enviar o horario para saber quem será o coordenador.
    private final int protAtualRelogio = 2;             //Atualizar relogio;
    private final int protIDPermitido = 3;              //Informa ao novo cliente que o id não é permitido.
    private final int protTesteDelay = 4;               //Protocolo para teste de delay da rede.
    private final int protTesteDelayResp = 41;          //Responde com os horarios para teste delay;
    //*************************************************************************************************************


    public Protocolo(IControlleConexao classe) {
        this.classe = classe;
    }
    
    /**
     * Metodo excutado sempre que chega uma nova mensagem.
     * @param str 
     */
    public void chegouMensagem(String str){
        seletorAcao(str.split(";"));
    }
    
    private void seletorAcao(String[] str) {
        int protocolo = Integer.parseInt(str[1].trim());
        switch (protocolo) {
            case protNovoCliente:
                System.out.println("Nova pessoa. Buscando ID disponivel");
                classe.novoRelogioR(str);
                break;
            case protIDPermitido:
                //idPemitido(str);
                break;
            case protEleicaoIniciar:
                classe.iniciarEleicaoR(str);
                break;
            case protEleicaoHorario:
                classe.enviarHorarioEleicaoR(str);
                break;
            case protAtualRelogio:
                classe.atualizarHorarioR(str);
                break;
            case protTesteDelay:
                classe.iniciarTesteDelayR(str);
                break;
            case protTesteDelayResp:
                classe.concluiTesteDelayR(str);
                break;
            default:
                classe.mensagemDesconhecida(str);
                break;
        }
    }
}
