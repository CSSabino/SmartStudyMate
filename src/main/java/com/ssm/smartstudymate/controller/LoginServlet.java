package com.ssm.smartstudymate.controller;

import com.ssm.smartstudymate.model.Docente;
import com.ssm.smartstudymate.model.DocenteDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

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
                session.setAttribute("success-login", "ok");
                address = "/WEB-INF/jsp/index.jsp";

            } else {
                request.setAttribute("success-login", "not ok");
                address = "/WEB-INF/login.jsp";
            }
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(address);

        dispatcher.forward(request, response);

    }
}

