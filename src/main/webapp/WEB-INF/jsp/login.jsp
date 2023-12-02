<%--
  Created by IntelliJ IDEA.
  User: utente
  Date: 23/11/2023
  Time: 15:42
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartStudyMate</title>
    <link rel="stylesheet" href="./css/login.css" type="text/css">
    <script type="text/javascript" src="./js/login.js" defer></script>
    <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon">
</head>
<body>

<div id="smartstudymate">
    <h1>Scopri SmartStudyMate, lo strumento intelligente nell'ambito dell'educazione!</h1>
    <h4>Se sei uno studente, hai la possibilità di interrogare le videolezioni, ricercare gli argomenti trattati nelle videolezioni, creare e rispondre a quiz.</h4>
    <h4>Se sei un insegnate, hai la possibilità di caricare le tue videolezioni, testare la bontà dell'applicazione sulle tue videolezioni, creare quiz ed esportali in formato Moodle XML (o, anche, in formato Aiken).</h4>
</div>
<div id="loginForm">
    <h2>Accesso SmartStudyMate</h2>
    <form action="login" method="get">
        <label class="label-form" for="accessCode">Codice di accesso (fornito dal docente):</label>
        <input class="input-form" type="text" id="accessCode" name="accessCode" placeholder="Inserisci il codice di accesso" required>
        <%
            String successAccessCode = (String) session.getAttribute("success-access");
            if(successAccessCode != null && successAccessCode.equalsIgnoreCase("not ok")){
        %>
        <span style="color: red">Codice di accesso inesistente. Riprova!</span>
        <%
            }
        %>
        <button type="submit">Entra</button>
    </form>

    <p><strong>Sei sei un docente, accedi con le tue credenziali.</strong></p>
    <form action="login" method="post">
        <label class="label-form" for="email">Email:</label>
        <input class="input-form" type="text" id="email" name="email" placeholder="Inserisci il tuo indirizzo mail" required>

        <label class="label-form" for="password">Password:</label>
        <input class="input-form" type="password" id="password" name="password" placeholder="Inserisci la tua password" required>

        <%
            String successLogin = (String) session.getAttribute("success-login");
            if(successLogin != null && successLogin.equalsIgnoreCase("not ok")){
        %>
        <span style="color: red">Credenziali errate. Riprova!</span>
        <%
            }
        %>
        <button type="submit">Accedi</button>
    </form>
</div>


<%
    String matching = (String) session.getAttribute("matching");
    System.out.println(matching);
    if(matching != null && matching.equalsIgnoreCase("true")){

%>
<div class="popup" id="popup">
    <div class="popup-content">
        <span class="close" id="closePopup">&times;</span>
        <h2>AZIONE RIUSCITA</h2>
        <p>Cambio password avvenuto correttamente!<br><br>Ora puoi accedere alla piattaforma inserendo le tue credenziali.</p>
    </div>
</div>
<%
    }
%>
</body>
</html>
