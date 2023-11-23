package com.ssm.smartstudymate.controller;

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

@WebServlet(name = "chatServlet", value = "/chat-servlet")
public class ChatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        String query = request.getParameter("query");
        String chatName = request.getParameter("lessonSelected");
        String chatResponse = "";

        Cronologia cronologia = (Cronologia) session.getAttribute("cronologia");

        cronologia.recuperaChat(chatName).inviaMessaggio(query);

        try {
            // Viene creato un oggetto URL che punta all'indirizzo http://localhost:5001/chatbot,
            // che è l'endpoint del server Python a cui vogliamo inviare la richiesta
            URL url = new URL("http://localhost:5001/chatbot");

            // Viene aperta una connessione HTTP al server utilizzando l'oggetto URL creato in precedenza.
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Viene impostato il metodo della richiesta come "POST", indicando che si sta inviando
            // una richiesta HTTP POST al server.
            conn.setRequestMethod("POST");

            // Imposta il flag per indicare che la connessione può essere utilizzata per l'output.
            conn.setDoOutput(true);

            // Preparazione dei dati da inviare al server Python: la query fornita dall'utente
            String dati = "query="+query;

            // I dati vengono convertiti in un array di byte usando la codifica UTF-8.
            byte[] postData = dati.getBytes();

            // Viene calcolata la lunghezza dei dati.
            int postDataLength = postData.length;

            // Viene impostato l'intestazione della richiesta HTTP per indicare la lunghezza dei dati inviati.
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            // Invio dei dati al server: Viene ottenuto un flusso di output dalla connessione e i
            // dati vengono scritti su di esso.
            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
            }

            System.out.println("Messaggio inviato al server Python!");


            // Viene creato un BufferedReader per leggere la risposta dal flusso di input della connessione.
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // Si creano una variabile per leggere ogni riga della risposta e un oggetto StringBuilder
            // per costruire la risposta completa.
            String line;
            StringBuilder resp = new StringBuilder();

            // Legge ogni riga della risposta e la aggiunge al StringBuilder `response`.
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            // Chiude il BufferedReader dopo aver completato la lettura della risposta.
            reader.close();

            System.out.println("Risposta dal server Python: risposta ricevuta da ChatGPT!");

            cronologia.recuperaChat(chatName).inviaMessaggio(resp.toString());

            chatResponse = resp.toString();

            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().append("[");
            response.getWriter().append("{\"response_bot\" : \"" + chatResponse + "\"}");
            response.getWriter().append("]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
