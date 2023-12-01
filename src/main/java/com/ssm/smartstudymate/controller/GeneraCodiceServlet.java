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

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

@WebServlet(name = "generaCodiceServlet", value = "/genera-codice")
public class GeneraCodiceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        Docente docente = (Docente) session.getAttribute("utente");
        VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
        ArrayList<Videolezione> videolezioni = videolezioneDAO.doRetrieveByDocente(docente);

        // Caratteri da prendere in cosiderazione
        String caratteri = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        // Lunghezza codice
        int lunghezza = 10;

        // Generazione della stringa casuale
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(lunghezza);

        // Genera la stringa carattere per carattere
        for (int i = 0; i < lunghezza; i++) {
            int randomIndex = random.nextInt(caratteri.length());
            char randomChar = caratteri.charAt(randomIndex);
            sb.append(randomChar);
        }

        String accessCode = sb.toString();

        System.out.println(accessCode);

        for(int i=0; i<videolezioni.size(); i++)
            videolezioneDAO.doSaveAccessCode(accessCode, docente, videolezioni.get(i));

        request.setAttribute("access-code", accessCode);


        String address = "WEB-INF/jsp/home.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response);

    }
}
