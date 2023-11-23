package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Chat;
import com.ssm.smartstudymate.model.Cronologia;
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

@WebServlet(name = "sceltaLezioneServlet", value = "/scelta-lezione")
public class SceltaLezioneServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String lezioneScelta = request.getParameter("lesson");

        HttpSession session = request.getSession();

        session.setAttribute("lesson-selected", lezioneScelta);

        Cronologia cronologia = (Cronologia) session.getAttribute("cronologia");

        // Verifica esistenza cronologia chat
        Boolean flag = false;

        if(cronologia != null) {
            if (cronologia.recuperaChat(lezioneScelta) != null){
                flag = true;
            }

        } else {
            cronologia = new Cronologia();
            session.setAttribute("cronologia", cronologia);
        }

        if (!flag){
            Chat chat = new Chat(lezioneScelta);
            cronologia.inserisciChat(lezioneScelta, chat);
        }

        try {

            URL url = new URL("http://localhost:5001/scegli_lezione");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String messaggio = "lesson="+lezioneScelta;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().append("[");
        String chatMessagges = "";

        if(cronologia.recuperaChat(lezioneScelta) != null){
            ArrayList<String> messagges = cronologia.recuperaChat(lezioneScelta).leggiMessaggi();

            if(!messagges.isEmpty()) {

                for (int i = 0; i < messagges.size(); i++) {
                    if (i % 2 == 0)
                        chatMessagges += "{\"userQuery\":\"" + messagges.get(i) + "\"},";
                    else
                        chatMessagges += "{\"botResponse\":\"" + messagges.get(i) + "\"},";
                }

                int lenText = chatMessagges.length();
                chatMessagges = chatMessagges.substring(0, lenText - 1);
            }

        }


        response.getWriter().append(chatMessagges);
        response.getWriter().append("]");

        session.setAttribute("cronologia", cronologia);
    }
}
