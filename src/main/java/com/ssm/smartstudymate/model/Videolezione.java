package com.ssm.smartstudymate.model;

import javax.print.Doc;

public class Videolezione {
    private String idVideoEmbeded;
    private String titolo;
    private String urlPhotoVideo;
    private String urlVideo;
    private String accessCode;
    private String descrizione;
    private Docente proprietario;

    public Videolezione(){}

    public Videolezione(String titolo, String urlVideo, String descrizione) {
        this.titolo = titolo;
        this.urlVideo = urlVideo;
        this.descrizione = descrizione;
    }

    public String getIdVideoEmbeded() {
        return idVideoEmbeded;
    }

    public void setIdVideoEmbeded(String idVideoEmbeded) {
        this.idVideoEmbeded = idVideoEmbeded;
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

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Docente getProprietario() {
        return proprietario;
    }

    public void setProprietario(Docente proprietario) {
        this.proprietario = proprietario;
    }
}
