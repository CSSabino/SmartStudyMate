package com.ssm.smartstudymate.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "downloadServlet", value = "/download-servlet")
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String nomeFile = request.getParameter("file");
        // Ottieni l'InputStream del file desiderato nella directory WEB-INF
        String filePath = "/WEB-INF/" + nomeFile;
        InputStream inputStream = getServletContext().getResourceAsStream(filePath);

        if (inputStream == null) {
            // Se il file non esiste, restituisci un errore 404 (Not Found)
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File non trovato");
            return;
        }

        // Imposta gli header per il download
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=myfile.txt");

        // Leggi il file e invia i dati alla risposta
        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, bytesRead);
        }

        inputStream.close();
    }
}
