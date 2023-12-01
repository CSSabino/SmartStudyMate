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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "sceltaPasswordServlet", value = "/scelta-password")
public class SceltaPasswordServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String address;

        HttpSession session = request.getSession();

        String regExPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#&@()_./–?]).{8,20}$";
        Pattern pattern = Pattern.compile(regExPassword);
        Matcher matcher = pattern.matcher(password);

        if(matcher.matches()){
            session.setAttribute("matching","true");
            DocenteDAO docenteDAO = new DocenteDAO();
            Docente docente = new Docente();
            docente.setEmail(email);
            docente.setPassword(password);
            docenteDAO.doSavePassword(docente);
            address = "/WEB-INF/jsp/login.jsp";
        } else {
            session.setAttribute("matching", "false");
            address = "/WEB-INF/jsp/password.jsp";
        }

        RequestDispatcher dispatcher =
                request.getRequestDispatcher(address);

        dispatcher.forward(request, response);
    }
}
