package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Videolezione;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@WebServlet(name = "searchTopic", value = "/search-topic")
public class SearchTopicServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String topic = request.getParameter("topic");
        String ripetizione = request.getParameter("ripetizione");

        try {

            URL url = new URL("http://localhost:5002/search-engine");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String parametroConAccenti = topic;
            String datiPost = URLEncoder.encode(parametroConAccenti, StandardCharsets.UTF_8.toString());
            String messaggio = "topic="+datiPost;
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

            System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");
            String chatbotResponse = resp.toString();

            // Chiamata all'endpoint per la ricezione dei minuti espressi in secondi
            URL url2 = new URL("http://localhost:5002/second-time");
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            conn2.setRequestMethod("POST");
            conn2.setDoOutput(true);

            String messaggio2 = "text="+chatbotResponse;
            byte[] postData2 = messaggio2.getBytes(StandardCharsets.UTF_8);
            int postDataLength2 = postData2.length;
            conn2.setRequestProperty("Content-Length", Integer.toString(postDataLength2));

            try (OutputStream os2 = conn2.getOutputStream()) {
                os2.write(postData2);
            }

            System.out.println("Messaggio inviato al server Python!");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
            String line2;
            StringBuilder resp2 = new StringBuilder();

            while ((line2 = reader2.readLine()) != null) {
                resp2.append(line2);
            }
            reader2.close();

            System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");
            String secondTime = resp2.toString();

            // Chiamata all'endpoint per la ricezione del numero della lezione
            URL url3 = new URL("http://localhost:5002/number_lesson");
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
            String numberLesson = resp3.toString();

            HttpSession session = request.getSession();
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
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().append("[");
            response.getWriter().append("{\"summarization\" : \"" + chatbotResponse + "\", \"url_video\":\"" + url_video + "\"}");
            response.getWriter().append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(ripetizione != null){
            String address = "WEB-INF/jsp/search_topic.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(address);
            dispatcher.forward(request, response);
        }
    }
}
