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

        String opzione1 = request.getParameter("opzione1");
        String opzione2 = request.getParameter("opzione2");
        String opzione3 = request.getParameter("opzione3");
        String opzione4 = request.getParameter("opzione4");
        String domanda = request.getParameter("domanda");
        String risposta = request.getParameter("risposta");

        // Preparazione dei dati da inviare al server Python: la query fornita dall'utente
        // Formattazione stringa per passaggio tramite connessine HTTP
        opzione1 = opzione1.replace("'", " ");
        opzione1 = opzione1.replace("\"", "");
        String parametroOpzione1 = opzione1;
        opzione2 = opzione2.replace("'", " ");
        opzione2 = opzione2.replace("\"", "");
        String parametroOpzione2 = opzione2;
        opzione3 = opzione3.replace("'", " ");
        opzione3 = opzione3.replace("\"", "");
        String parametroOpzione3 = opzione3;
        opzione4 = opzione4.replace("'", " ");
        opzione4 = opzione4.replace("\"", "");
        String parametroOpzione4 = opzione4;
        domanda = domanda.replace("'", " ");
        domanda = domanda.replace("\"", "");
        String parametroDomanda = domanda;
        risposta = risposta.replace("'", " ");
        risposta = risposta.replace("\"", "");
        String parametroRisposta = risposta;
        String datiOpzione1 = URLEncoder.encode(parametroOpzione1, StandardCharsets.UTF_8.toString());
        String datiOpzione2 = URLEncoder.encode(parametroOpzione2, StandardCharsets.UTF_8.toString());
        String datiOpzione3 = URLEncoder.encode(parametroOpzione3, StandardCharsets.UTF_8.toString());
        String datiOpzione4 = URLEncoder.encode(parametroOpzione4, StandardCharsets.UTF_8.toString());
        String datiDomanda = URLEncoder.encode(parametroDomanda, StandardCharsets.UTF_8.toString());
        String datiRisposta = URLEncoder.encode(parametroRisposta, StandardCharsets.UTF_8.toString());

        HttpSession session = request.getSession();
        session.removeAttribute("codeForQuiz");

        try {
            URL url = new URL("http://localhost:5001/valutaRisposta");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String messaggio = "opzione1=" + datiOpzione1 + "&opzione2=" + datiOpzione2 + "&opzione3="
                    + datiOpzione3 + "&opzione4=" + datiOpzione4 + "&domanda="
                    + datiDomanda+"&risposta="+datiRisposta;
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
            response.getWriter().append("{\"valutazione\" : \"" + valutazione + "\"}");
            response.getWriter().append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
