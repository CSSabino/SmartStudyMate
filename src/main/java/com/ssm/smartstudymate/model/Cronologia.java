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

    public void inserisciChat(String urlLezione, Chat chat){
        cronologia.put(urlLezione, chat);
    }

    public Chat recuperaChat(String urlLezione){
        return cronologia.get(urlLezione);
    }

    public int numeroChat(){
        return cronologia.size();
    }
}
