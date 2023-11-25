package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Videolezione;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@WebServlet(name = "valutaRispostaServlet", value = "/valuta-quiz")
public class ValutaRispostaServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String domanda = request.getParameter("domanda");
        String risposta = request.getParameter("risposta");

        domanda = domanda.replace("'", " ");
        domanda = domanda.replace("\"", "");
        String parametroDomanda = domanda;
        risposta = risposta.replace("'", " ");
        risposta = risposta.replace("\"", "");
        String parametroRisposta = risposta;
        String datiDomanda = URLEncoder.encode(parametroDomanda, StandardCharsets.UTF_8.toString());
        String datiRisposta = URLEncoder.encode(parametroRisposta, StandardCharsets.UTF_8.toString());

        HttpSession session = request.getSession();
        session.removeAttribute("codeForQuiz");

        try {
            URL url = new URL("http://localhost:5001/valutaRisposta");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String messaggio = "domanda="+datiDomanda+"&risposta="+datiRisposta;
            byte[] postData = messaggio.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

            System.out.println("Messaggio inviato al server Python!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder resp = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }
            reader.close();

            String valutazione = resp.toString();
            System.out.println(valutazione+"!");

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().append("[");
/*
            if(valutazione.equalsIgnoreCase("errata")){

                try {

                    URL url2 = new URL("http://localhost:5002/search-engine");
                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                    conn2.setRequestMethod("POST");
                    conn2.setDoOutput(true);


                    String messaggio2 = "topic="+datiDomanda;
                    byte[] postData2 = messaggio2.getBytes(StandardCharsets.UTF_8);
                    int postDataLength2 = postData2.length;
                    conn2.setRequestProperty("Content-Length", Integer.toString(postDataLength2));

                    try (OutputStream os2 = conn2.getOutputStream()) {
                        os2.write(postData2);
                    }

                    System.out.println("Messaggio inviato al server Python!");
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8));
                    String line2;
                    StringBuilder resp2 = new StringBuilder();

                    while ((line2 = reader2.readLine()) != null) {
                        resp2.append(line2);
                    }
                    reader2.close();

                    System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");
                    String chatbotResponse = resp2.toString();

                    // Chiamata all'endpoint per la ricezione dei minuti espressi in secondi
                    URL url3 = new URL("http://localhost:5002/second-time");
                    HttpURLConnection conn3 = (HttpURLConnection) url3.openConnection();
                    conn3.setRequestMethod("POST");
                    conn3.setDoOutput(true);

                    String messaggio3 = "text="+chatbotResponse;
                    byte[] postData3 = messaggio3.getBytes(StandardCharsets.UTF_8);
                    int postDataLength3 = postData3.length;
                    conn3.setRequestProperty("Content-Length", Integer.toString(postDataLength3));

                    try (OutputStream os3 = conn3.getOutputStream()) {
                        os3.write(postData3);
                    }

                    System.out.println("Messaggio inviato al server Python!");
                    BufferedReader reader3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
                    String line3;
                    StringBuilder resp3 = new StringBuilder();

                    while ((line3 = reader3.readLine()) != null) {
                        resp3.append(line3);
                    }
                    reader3.close();

                    System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");
                    String secondTime = resp3.toString();

                    // Chiamata all'endpoint per la ricezione del numero della lezione
                    URL url4 = new URL("http://localhost:5002/number_lesson");
                    HttpURLConnection conn4 = (HttpURLConnection) url4.openConnection();
                    conn4.setRequestMethod("POST");
                    conn4.setDoOutput(true);

                    String messaggio4 = "text="+chatbotResponse;
                    byte[] postData4 = messaggio4.getBytes(StandardCharsets.UTF_8);
                    int postDataLength4 = postData4.length;
                    conn4.setRequestProperty("Content-Length", Integer.toString(postDataLength4));

                    try (OutputStream os4 = conn4.getOutputStream()) {
                        os4.write(postData4);
                    }

                    System.out.println("Messaggio inviato al server Python!");
                    BufferedReader reader4 = new BufferedReader(new InputStreamReader(conn4.getInputStream()));
                    String line4;
                    StringBuilder resp4 = new StringBuilder();

                    while ((line4 = reader4.readLine()) != null) {
                        resp3.append(line4);
                    }
                    reader4.close();

                    System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");
                    String numberLesson = resp4.toString();

                    ArrayList<Videolezione> playlist = (ArrayList<Videolezione>) session.getAttribute("videolezioni");
                    String url_video = "";

                    if(playlist != null){
                        Videolezione videolezione = playlist.get(Integer.parseInt(numberLesson));
                        url_video = videolezione.getIdVideoEmbeded() + "?start=" + secondTime.substring(0,secondTime.length()-1);
                    }

                    session.setAttribute("search_done", "true");
                    session.setAttribute("summary", chatbotResponse);
                    session.setAttribute("url_video_embeded", url_video);

                    chatbotResponse = chatbotResponse.replace("\"", "");

                    response.getWriter().append("{\"valutazione\" : \"" + valutazione + "\", \"summarization\" : \"" + chatbotResponse + "\", \"url_video\":\"" + url_video + "\"}");


                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                session.setAttribute("valutazione", valutazione);
                response.getWriter().append("{\"valutazione\" : \"" + valutazione + "\"}");
            }
            */
            response.getWriter().append("{\"valutazione\" : \"" + valutazione + "\"}");
            response.getWriter().append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
