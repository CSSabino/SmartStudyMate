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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
        String tipoQuiz = request.getParameter("tipoQuiz");
        String quizAiken = "";
        String quizMoodle = "";
        HttpSession session = request.getSession();

        if(truefalse.equalsIgnoreCase("0") && matching.equalsIgnoreCase("0")
                && shortAnswer.equalsIgnoreCase("0") && numerical.equalsIgnoreCase("0")
        && essay.equalsIgnoreCase("0")){
            try {
                URL url = null;
                VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
                String argomenti = "";

                if(tipoQuiz != null && tipoQuiz.equalsIgnoreCase("generale")){
                    Docente docente = (Docente) session.getAttribute("utente");

                    ArrayList<Videolezione> videolezioni = videolezioneDAO.doRetrieveByDocente(docente);

                    for(int i=0; i<videolezioni.size(); i++)
                        argomenti += videolezioni.get(i).getDescrizione() + " ";

                    url = new URL("http://localhost:5002/quizAiken");
                }
                else {
                    String lesson_selected = (String) session.getAttribute("lesson-selected");

                    Videolezione videolezione = videolezioneDAO.doRetrieveByUrl(lesson_selected);

                    argomenti = videolezione.getDescrizione();

                    url = new URL("http://localhost:5001/quizAiken");
                }

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Preparazione dei dati da inviare al server Python: la query fornita dall'utente
                // Formattazione stringa per passaggio tramite connessine HTTP
                argomenti = argomenti.replace("'", " ");
                argomenti = argomenti.replace("\"", "");
                String parametroConAccenti = argomenti;
                String datiPost = URLEncoder.encode(parametroConAccenti, StandardCharsets.UTF_8.toString());

                String messaggio = "multiple=" + multiple + "&argomenti=" + datiPost;
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

                    // Aggiungi una nuova riga al termine di ogni linea letta
                    resp.append(System.lineSeparator());
                }
                reader.close();

                quizAiken = resp.toString();
                quizAiken = quizAiken.replace("    ", "");
                creaFile(quizAiken, "aiken.txt");
                System.out.println(quizAiken);

                session.setAttribute("quizaiken", "true");

                // aggiunta sequenza di escape per passagio string a con JSON
                quizAiken = quizAiken.replace("\"", "\\\"");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {

            URL url = null;
            VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
            String argomenti = "";

            if(tipoQuiz != null && tipoQuiz.equalsIgnoreCase("generale")){
                Docente docente = (Docente) session.getAttribute("utente");

                ArrayList<Videolezione> videolezioni = videolezioneDAO.doRetrieveByDocente(docente);

                for(int i=0; i<videolezioni.size(); i++)
                    argomenti += videolezioni.get(i).getDescrizione() + " ";

                url = new URL("http://localhost:5002/quizMoodle");
            }
            else {
                String lesson_selected = (String) session.getAttribute("lesson-selected");

                Videolezione videolezione = videolezioneDAO.doRetrieveByUrl(lesson_selected);

                argomenti = videolezione.getDescrizione();

                url = new URL("http://localhost:5001/quizMoodle");
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Preparazione dei dati da inviare al server Python: la query fornita dall'utente
            // Formattazione stringa per passaggio tramite connessine HTTP
            argomenti = argomenti.replace("'", " ");
            argomenti = argomenti.replace("\"", "");
            String parametroConAccenti = argomenti;
            String datiPost = URLEncoder.encode(parametroConAccenti, StandardCharsets.UTF_8.toString());

            String messaggio = "multiple=" + multiple + "&truefalse=" + truefalse + "&matching=" + matching +
                    "&shortanswer=" + shortAnswer + "&numerical=" + numerical + "&essay=" + essay +
                    "&argomenti=" + datiPost;
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

                // Aggiungi una nuova riga al termine di ogni linea letta
                resp.append(System.lineSeparator());
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
            response.getWriter().append("{\"quiz_moodle\" : \"true\", \"quiz_aiken\" : \"false\"}");
        else
            response.getWriter().append("{\"quiz_moodle\" : \"true\", \"quiz_aiken\" : \"true\"}");

        response.getWriter().append("]");
        session.setAttribute("download-disponibile", "true");

    }

    public void creaFile(String content, String nomeFile) throws IOException {
        // Recupera il percorso della directory di destinazione nel contesto dell'applicazione
        String destinationDir = getServletContext().getRealPath("/WEB-INF");

        // Crea il percorso completo del file
        String filePath = destinationDir + File.separator + nomeFile;

        // Scrivi nel file utilizzando BufferedWriter e FileWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            // Scrivi il contenuto nel file
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore");
        }

        // Rispondi con un messaggio di successo
        System.out.println("File creato con successo: " + filePath);
    }
}
