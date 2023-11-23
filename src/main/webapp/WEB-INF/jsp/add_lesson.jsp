<%--
  Created by IntelliJ IDEA.
  User: utente
  Date: 23/11/2023
  Time: 17:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartStudyMate</title>
    <link rel="stylesheet" href="./css/login.css" type="text/css">
</head>
<body>

<div id="videolezioneForm">
    <h2>Inserisci una videolezione sulla piattaforma</h2>

    <form action="aggiungi-lezione" method="post">

        <label for="titolo">Titolo videolezione:</label>
        <input type="text" id="titolo" name="titolo" placeholder="Inserisci titolo della videolezione">
        <label for="url">Url videolezione:</label>
        <input type="text" id="url" name="url" placeholder="Inserisci URL della videolezione">
        <label for="keytopic">Key-topic videolezione:</label>
        <input type="text" id="keytopic" name="keytopic" placeholder="Inserisci gli argomenti trattati nella videolezione">

        <%
            String presence = (String) session.getAttribute("lesson-presence");
            if(presence != null && presence.equalsIgnoreCase("true")){
        %>
        <p style="color: red">Esiste già nella piattaforma una videolezione con questo URL. Inserisci una nuova videolezione!</p>
        <%
            }
        %>

        <button type="submit">Inserisci videolezione</button>
        <a href="router-servlet?filejsp=home.jsp">Torna all'home</a>
    </form>
</div>

</body>
</html>
