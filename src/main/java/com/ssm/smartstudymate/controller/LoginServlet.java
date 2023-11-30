package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Docente;
import com.ssm.smartstudymate.model.DocenteDAO;
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

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        DocenteDAO docenteDAO = new DocenteDAO();

        HttpSession session = request.getSession();

        String address;

        if(docenteDAO.isPrimoAccesso(email)){
            session.setAttribute("primo_accesso","ok");
            System.out.println("Primo accesso");
            session.setAttribute("email", email);
            address = "/WEB-INF/jsp/password.jsp";
        } else {

            //dovremmo recuperare i video di un docente ma adesso ci concentriamo sul login

            Docente docente = new Docente();
            docente.setEmail(email);
            docente.setPassword(password);


            docente = docenteDAO.doRetrieveByEmailPassword(docente);

            if (docente != null) {
                session.setAttribute("utente", docente);
                VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
                ArrayList<Videolezione> videolezioni = videolezioneDAO.doRetrieveByDocente(docente);
                session.setAttribute("videolezioni", videolezioni);

                if(!videolezioni.isEmpty()){
                    String lastUrl = "no";
                    for(int i=0; i< videolezioni.size(); i++){
                        String urlVideolezione = videolezioni.get(i).getUrlVideo();
                        if(i == videolezioni.size() - 1)
                            lastUrl = "si";

                        try {
                            URL url = new URL("http://localhost:5002/initialize_list_urls");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);

                            String messaggio = "lesson=" + urlVideolezione + "&last=" + lastUrl;
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
                    }
                }

                session.setAttribute("success-login", "ok");
                address = "router-servlet?filejsp=home.jsp";

            } else {
                session.removeAttribute("success-access");
                session.setAttribute("success-login", "not ok");
                address = "/WEB-INF/jsp/login.jsp";
            }
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(address);

        dispatcher.forward(request, response);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accessCode = request.getParameter("accessCode");

        HttpSession session = request.getSession();
        String address;
        VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
        ArrayList<Videolezione> videolezioni = videolezioneDAO.doRetrieveByAccessCode(accessCode);

        if(videolezioni != null && !(videolezioni.isEmpty())){
            session.setAttribute("videolezioni", videolezioni);
            session.setAttribute("success-access", "ok");
            address = "router-servlet?filejsp=home.jsp";
        } else {
            session.removeAttribute("success-login");
            session.setAttribute("success-access", "not ok");
            address = "/WEB-INF/jsp/login.jsp";
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(address);

        dispatcher.forward(request, response);
    }
}

