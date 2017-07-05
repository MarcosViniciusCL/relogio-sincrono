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
public class Relogio implements Comparable<Relogio>{
    private long horario;
    private int id;

    public Relogio(long horario, int id) {
        this.horario = horario;
        this.id = id;
    }

    public long getHorario() {
        return horario;
    }

    public void setHorario(long horario) {
        this.horario = horario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Relogio o) {
        if(this.horario > o.getHorario()){
            return -1;
        } else if(this.horario < o.getHorario()){
            return 1;
        } else {
            if(this.id > o.getId()){
                return -1;
            } else if(this.id < o.getId()){
                return 1;
            }
            return 0;
        }
    }
    
    
}
