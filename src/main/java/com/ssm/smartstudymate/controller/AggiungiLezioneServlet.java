package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Docente;
import com.ssm.smartstudymate.model.Videolezione;
import com.ssm.smartstudymate.model.VideolezioneDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@WebServlet(name = "aggiungiLezioneServlet", value = "/aggiungi-lezione")
public class AggiungiLezioneServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String titoloVideolezione = request.getParameter("titolo");
        String urlVideolezione = request.getParameter("url");
        String descrizioneVideolezione = request.getParameter("keytopic");

        HttpSession session = request.getSession();
        Docente docente = (Docente) session.getAttribute("utente");
        String address = "";

        if (docente != null) {

            VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
            Videolezione videolezione = new Videolezione(titoloVideolezione, urlVideolezione, descrizioneVideolezione);

            if (videolezioneDAO.doRetrieveByUrl(urlVideolezione) == null) {

                session.setAttribute("lesson-presence", "false");

                try {

                    URL url = new URL("http://localhost:5001/returnUrlPhoto");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String messaggio = "lesson=" + urlVideolezione;
                    byte[] postData = messaggio.getBytes(StandardCharsets.UTF_8);
                    int postDataLength = postData.length;
                    conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(postData);
                    }

                    System.out.println("Messaggio inviato al server Python!");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder resp = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        resp.append(line);
                    }
                    reader.close();

                    System.out.println("Risposta dal server Python: " + resp.toString());

                    videolezione.setUrlPhotoVideo(resp.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                videolezioneDAO.doSave(videolezione, docente);

                ArrayList<Videolezione> playlist = videolezioneDAO.doRetrieveByDocente(docente);

                session.setAttribute("videolezioni", playlist);

                address = "/WEB-INF/jsp/home.jsp";

            } else {
                session.setAttribute("lesson-presence", "true");
                address = "/WEB-INF/jsp/add_lesson.jsp";
            }

        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(address);

        dispatcher.forward(request, response);
    }
}
