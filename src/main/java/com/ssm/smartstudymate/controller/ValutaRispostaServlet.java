package com.ssm.smartstudymate.controller;

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

        HttpSession session = request.getSession();

        try {
            URL url = new URL("http://localhost:5001/valutaRisposta");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String datiDomanda = URLEncoder.encode(parametroDomanda, StandardCharsets.UTF_8.toString());
            String datiRisposta = URLEncoder.encode(parametroRisposta, StandardCharsets.UTF_8.toString());
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

            if(valutazione.equalsIgnoreCase("errata")){

            } else {
                session.setAttribute("valutazione", valutazione);
            }



            // aggiunta sequenza di escape per passagio string a con JSON

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().append("[");
            response.getWriter().append("{\"valutazione\" : \"" + valutazione + "\"}");
            response.getWriter().append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
