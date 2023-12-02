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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@WebServlet(name="logout-servlet", value="/logoutServlet")
public class LogoutServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session=request.getSession();

        try {
            URL url = new URL("http://localhost:5002/logout");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String messaggio = "request=logout";
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


        session.invalidate();

        response.sendRedirect("./index.html");
    }
}
