package com.ssm.smartstudymate.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "routerServlet", value = "/router-servlet")
public class RouterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileJsp = request.getParameter("filejsp");

        String quizGenerale = request.getParameter("quiz_generale");

        String address = "WEB-INF/jsp/" + fileJsp;

        request.getSession().removeAttribute("quizaiken");
        request.getSession().removeAttribute("quizmoodle");
        request.getSession().removeAttribute("download-disponibile");
        request.getSession().removeAttribute("quiz_generale");

        if(quizGenerale != null && quizGenerale.equalsIgnoreCase("true")){
            request.getSession().setAttribute("quiz_generale", "true");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileJsp = request.getParameter("filejsp");

        String address = "WEB-INF/jsp/" + fileJsp;

        RequestDispatcher dispatcher = request.getRequestDispatcher(address);
        dispatcher.forward(request, response);
    }
}
