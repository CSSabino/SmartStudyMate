package com.ssm.smartstudymate.model;

import java.util.ArrayList;

public class Chat {
    private String nomeChat;
    private ArrayList<String> messaggi;

    public Chat(String nomeChat) {
        this.nomeChat = nomeChat;
        this.messaggi = new ArrayList<>();
    }

    public String getNomeChat() {
        return nomeChat;
    }

    public void inviaMessaggio(String messaggio) {
        messaggi.add(messaggio);
    }

    public ArrayList<String> leggiMessaggi() {
        return messaggi;
    }

    public void cancellaMessaggi() {
        messaggi.clear();
    }

    public int numeroMessaggi() {
        return messaggi.size();
    }
}
