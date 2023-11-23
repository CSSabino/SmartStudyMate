package com.ssm.smartstudymate.model;

public class Videolezione {
    private String titolo;
    private String urlPhotoVideo;
    private String urlVideo;
    private String descrizione;

    public Videolezione(){}

    public Videolezione(String titolo, String urlVideo, String descrizione) {
        this.titolo = titolo;
        this.urlVideo = urlVideo;
        this.descrizione = descrizione;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getUrlPhotoVideo() {
        return urlPhotoVideo;
    }

    public void setUrlPhotoVideo(String urlPhotoVideo) {
        this.urlPhotoVideo = urlPhotoVideo;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}
