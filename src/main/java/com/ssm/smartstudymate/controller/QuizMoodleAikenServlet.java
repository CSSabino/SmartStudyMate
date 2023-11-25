package com.ssm.smartstudymate.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "quizMoodleAikenServlet", value = "/quizmoodleaiken-servlet")
public class QuizMoodleAikenServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String multiple = request.getParameter("multiple_choice");
        String truefalse = request.getParameter("true_false");
        String matching = request.getParameter("matching");
        String shortAnswer = request.getParameter("short_answer");
        String numerical = request.getParameter("numerical");
        String essay = request.getParameter("essay");
        String quizAiken = "";
        String quizMoodle = "";
        HttpSession session = request.getSession();

        if(truefalse.equalsIgnoreCase("0") && matching.equalsIgnoreCase("0")
                && shortAnswer.equalsIgnoreCase("0") && numerical.equalsIgnoreCase("0")
        && essay.equalsIgnoreCase("0")){
            try {
                URL url = new URL("http://localhost:5001/quizAiken");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String messaggio = "multiple=" + multiple;
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

                quizAiken = resp.toString();
                creaFile(quizAiken, "aiken.txt");
                System.out.println(quizAiken);

                session.setAttribute("quizaiken", quizAiken);

                // aggiunta sequenza di escape per passagio string a con JSON
                quizAiken = quizAiken.replace("\"", "\\\"");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            URL url = new URL("http://localhost:5001/quizMoodle");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String messaggio = "multiple=" + multiple + "&truefalse=" + truefalse + "&matching=" + matching +
                    "&shortanswer=" + shortAnswer + "&numerical=" + numerical + "&essay=" + essay;
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

            quizMoodle = resp.toString();
            creaFile(quizMoodle, "moodle.xml");
            System.out.println(quizMoodle);

            session.setAttribute("quizmoodle", "true");

            // aggiunta sequenza di escape per passagio string a con JSON
            quizMoodle = quizMoodle.replace("\"", "\\\"");




        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().append("[");

        if(quizAiken.isEmpty())
            response.getWriter().append("{\"quiz_moodle\" : \"" + quizMoodle + "\"}");
        else
            response.getWriter().append("{\"quiz_moodle\" : \"" + quizMoodle + "\", \"quiz_aiken\" : \"" + quizAiken + "\"}");

        response.getWriter().append("]");
        session.setAttribute("download-disponibile", "true");

        String address = "router-servlet?filejsp=form_quiz";

        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response);

    }

    public void creaFile(String content, String nomeFile) throws IOException {
        // Recupera il percorso della directory di destinazione nel contesto dell'applicazione
        String destinationDir = getServletContext().getRealPath("/WEB-INF");

        // Crea il percorso completo del file
        String filePath = destinationDir + File.separator + nomeFile;

        // Crea il file e scrivi dei dati di esempio
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore durante la creazione del file.");
            return;
        }

        // Rispondi con un messaggio di successo
        System.out.println("File creato con successo: " + filePath);
    }
}
