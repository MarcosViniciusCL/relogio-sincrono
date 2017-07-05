/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pbl.controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import pbl.model.IControlleConexao;
import pbl.model.Relogio;
import pbl.model.IControllerRelogio;

/**
 *
 * @author marcos
 */
public class ControllerConexao implements IControlleConexao{
    
    private final Protocolo protocolo;
    
    private final int porta = 4321;
    private final String endGrupo = "239.0.0.1";
    private MulticastSocket grupoMulticast;
    private Thread monitorMensGRP;
    private final IControllerRelogio controllerRelogio;
    private boolean coordenador = false;
    private boolean podeEnviar = true;
    private int identificador = 0;
    private final List<Relogio> listRelogio; //Lista de todos clientes para eleição.

    //********************************** PROTOCOLOS DE REDE *******************************************************
    private final int protGenerico = Integer.MAX_VALUE;
    private final int protNovoCliente = 0;              //Informa que existe mais um novo cliente no grupo;
    private final int protEleicaoIniciar = 1;           // Iniciar eleição de coordenador;
    private final int protEleicaoHorario = 10;          //Quando a eleição é iniciar todos enviar o horario para saber quem será o coordenador.
    private final int protAtualRelogio = 2;             //Atualizar relogio;
    private final int protIDPermitido = 3;              //Informa ao novo cliente que o id não é permitido.
    private final int protTesteDelay = 4;               //Protocolo para teste de delay da rede.
    //*************************************************************************************************************

    //******************************** VARIAVEIS DE REDE *******************************************************
    private final int tempoReenvio = 3; //Tempo em segundos para atualizar o horario caso seja o coordenador
    private int auxTempoReenvio;
    private final int timeout = 10; //Tempo em segundos que o clientes deve aguardar receber uma atualização;
    private int auxTimeout = 0;
    private long delayRede = 0;
    //***********************************************************************************************************

    private long maiorHorario = 0;
    private long meuHorario = 0;
    private int idMaior = -1;
    
    public ControllerConexao(IControllerRelogio controllerConexao) {
        this.controllerRelogio = controllerConexao;
        this.coordenador = false;
        this.protocolo = new Protocolo(this);
        setID(new Random().nextInt(1000));
        this.listRelogio = new ArrayList();
        assinarGrupoMult();
        monitorMensGRP();
    }

    //******************************************** ENVIO DE MENSAGEM ************************************** 
    public void atualizarHorario(long horario) {
        meuHorario = horario;
        System.out.print(".");
        enviarMensagemGRP(protAtualRelogio + ";" + horario);
    }
    
    public void iniciarEleicao() {
        enviarMensagemGRP(protEleicaoIniciar + "");
    }
    
    public void enviarHorarioEleicao() {
        long auxMeuHorario = controllerRelogio.getHorario();
        this.meuHorario = auxMeuHorario; //Salvo o meu horario para comparação futuras.
        enviarMensagemGRP(protEleicaoHorario + ";" + auxMeuHorario);
    }
    
    public void novoRelogio() {
        pause(1000);
        enviarMensagemGRP(protNovoCliente + "");
        iniciarEleicao();
    }
    
    public void iniciarTesteDelay() {
        permitirEnvio(false); //<-- Impedi que qualquer mensagem seja enviada pela rede deste host
        long inicio;
        long fim;
        String[] resp;
        
        inicio = System.currentTimeMillis();
        enviarMensagemGRP(protTesteDelay + "");
        resp = receberMensagemGRP().split(";");
        fim = System.currentTimeMillis();
        
        if (Integer.parseInt(resp[1].trim()) == protTesteDelay) {
            this.delayRede = fim - inicio;
            this.delayRede /= 2;
            System.out.println("Delay da rede: " + delayRede + "ms");
        }
        
        permitirEnvio(true); //<-- Libera o envio de mensagens deste cliente.
    }
    
    public void solicitarID() {
        enviarMensagemGRP(protNovoCliente + "");
    }

    //********************************** RECEBIMENTO MENSAGEM **********************************************
    public void atualizarHorarioR(String[] str) {
        int idJog = Integer.parseInt(str[0]);
        long tempH = Long.parseLong(str[2].trim());
        zerarTimeout(); // <-- Sempre que recebe uma mensagem de atualização do relogio, zera o timeout.
        zerarTempoReenvio(); // <-- Zerar tempo de reenvio de uma mensagem.(Só o coordenador pode enviar mensagem de atualização do relogio.)
        testarDuplicidade(idJog, tempH);
        if (!testarErroHoraPassado(tempH)) {  //Verifica se o coordenador está com horario no futuro.
            controllerRelogio.setHorario(tempH + this.delayRede / 1000);
        }
    }
    
    public void iniciarEleicaoR(String[] str) {
        listRelogio.clear();
        zerarTimeout();
        enviarHorarioEleicao(); //Quando recebe uma mensagem de eleição, envia ao grupo seu horario atual para fazer a eleição.
    }
    
    public void enviarHorarioEleicaoR(String[] str) {
        int idJog = Integer.parseInt(str[0]);
        long tempH = Long.parseLong(str[2].trim());
        zerarTempoReenvio();
        listRelogio.add(new Relogio(tempH, idJog));
        Collections.sort(listRelogio);
        setCoordenador(listRelogio.get(0).getId() == this.identificador && listRelogio.get(0).getHorario() == this.meuHorario);
    }
    
    public void novoRelogioR(String[] str) {
        int idNovoJog = Integer.parseInt(str[0]);
        System.out.println("Há um novo jogador. Nova eleição será iniciada.");
    }
    
    public void iniciarTesteDelayR() {
        permitirEnvio(false);
        pause(500);
        permitirEnvio(true);
    }
    
    public void idPemitido(String[] str) {
        int idJog = Integer.parseInt(str[0]);
        if (idJog == identificador && this.idMaior == -1) {
            identificador++;
            novoRelogio();
        }
        if (idJog > this.idMaior) {
            this.idMaior = idJog;
        }
    }

    /**
     * ******************************************************************************************************
     */


    /**
     * Assina um grupo multcast.
     *
     * @param endGrupo - Endereço referente ao grupo.
     * @param porta - Porta do grupo multcast.
     * @throws IOException
     */
    private void assinarGrupoMult() {
        try {
            this.grupoMulticast = new MulticastSocket(this.porta); //Criando instância de grupo. 
            grupoMulticast.joinGroup(InetAddress.getByName(this.endGrupo)); //Entrando no grupo de multicast
            novoRelogio();
            iniciarTimeout();
        } catch (IOException ex) {
            Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Envia a mensagem para todos que estão no grupo de multicast.
     *
     * @param mens - Mensagem que será enviada ao grupo.
     */
    private void enviarMensagemGRP(String mens) {
        while (!podeEnviar) {
            pause(200);
        }
        mens = identificador + ";" + mens;
        if (mens != null) {
            DatagramPacket env;
            try {
                env = new DatagramPacket(mens.getBytes(), mens.length(), InetAddress.getByName(this.endGrupo), this.porta);
                grupoMulticast.send(env);
            } catch (UnknownHostException ex) {
                Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    /**
     * Ativa o monitoramento no grupo multcast e informa quando há uma nova
     * mensagem.
     */
    private void monitorMensGRP() {
        if (grupoMulticast != null) {
            monitorMensGRP = new Thread() {
                @Override
                public void run() {
                    byte[] buff;
                    DatagramPacket mens;
                    while (true) {
                        buff = new byte[1024];
                        mens = new DatagramPacket(buff, buff.length);
                        try {
                            grupoMulticast.receive(mens);
                            mensagemRecebida(new String(mens.getData()));
                        } catch (IOException ex) {
                            Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
            monitorMensGRP.start();
        }
    }

    /**
     * Recebe mensagens do grupo multicast.
     *
     * @throws IOException
     */
    private String receberMensagemGRP() {
        try {
            if (grupoMulticast != null) {
                byte buff[] = new byte[1024];
                DatagramPacket mens = new DatagramPacket(buff, buff.length);
                grupoMulticast.receive(mens);
                return new String(mens.getData());
            }
        } catch (IOException ex) {
            Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void mensagemRecebida(String string) {
        protocolo.chegouMensagem(string);
    }
    
    private void iniciarTimeout() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            int auxTimeoutDelay = 0; //<-- Tempo para testar o delay da rede.

            @Override
            public void run() {
                auxTimeout++;
                auxTempoReenvio++;
                auxTimeoutDelay++;
                
                if (auxTempoReenvio == tempoReenvio && coordenador) {
                    atualizarHorario(controllerRelogio.getHorario());
                }
                if (auxTimeout == timeout) {
                    System.out.println("Não há uma nova mensagem a " + timeout + " segundos. Convocando uma eleição.");
                    iniciarEleicao();
                }
                if (auxTimeoutDelay == 60) { //Atualizar o delay a cada 60 seg
                    if (coordenador) {
                        iniciarTesteDelay();
                    }
                    auxTimeoutDelay = 0;
                }
            }
        }, 0,
                1000 //tempo, 1000ms = 1segundo
        );
    }
    
    private void testarDuplicidade(int idJog, long horario) {
        if (idJog == this.identificador && this.meuHorario != horario) {
            System.out.println("Existe ID igual o meu na rede. Trocando ID.");
            setID(this.identificador++);
        }
    }
    
    private void zerarTimeout() {
        this.auxTimeout = 0;
    }
    
    private void zerarTempoReenvio() {
        this.auxTempoReenvio = 0;
    }
    
    private void setCoordenador(boolean b) {
        this.coordenador = b;
        controllerRelogio.setCoordenador(b);
        if (b) {
            System.out.println("Sou o novo coordenador.");
        }
    }
    
    private boolean testarErroHoraPassado(long tempH) {
        if (tempH < controllerRelogio.getHorario()) {
            System.out.println("Erro no coordenador. Solicitando eleição.");
            iniciarEleicao();
            return true;
        }
        return false;
    }
    
    private void setID(int id) {
        this.identificador = id;
        controllerRelogio.setID(id);
    }
    
    private void pause(long milisegundo) {
        try {
            Thread.sleep(milisegundo);
        } catch (InterruptedException ex) {
            Logger.getLogger(ControllerConexao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void permitirEnvio(boolean b) {
        this.podeEnviar = b;
    }

    @Override
    public void mensagemDesconhecida(String[] str) {
        controllerRelogio.novaMensagemRecebida(str);
    }
    
}
