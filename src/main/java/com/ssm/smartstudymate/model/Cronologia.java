package com.ssm.smartstudymate.model;

import java.util.HashMap;
import java.util.Map;

public class Cronologia {
    private Map<String, Chat> cronologia;

    public Cronologia() {
        this.cronologia = new HashMap<>();
    }

    public Map<String, Chat> getHistoryChat() {
        return cronologia;
    }

    public void inserisciChat(String lezione, Chat chat){
        cronologia.put(lezione, chat);
    }

    public Chat recuparaChat(String lezione){
        return cronologia.get(lezione);
    }

    public int numeroChat(){
        return cronologia.size();
    }
}
