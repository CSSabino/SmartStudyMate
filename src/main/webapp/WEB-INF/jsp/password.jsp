<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartStudyMate</title>
    <link rel="stylesheet" href="./css/login.css" type="text/css">
</head>
<body>

<%
    String email = (String) session.getAttribute("email");
%>
<div id="passwordForm">
    <h2>Scegli la tua password</h2>

    <form action="scelta-password" method="post">

        <label class="label-form" for="password">Password:</label>
        <input class="input-form" type="password" id="password" name="password" placeholder="Inserisci la tua password">
        <input type="hidden" name="email" value="<%=email%>">
        <p>La password deve essere lunga almeno 8 caratteri. Deve contenere almeno una lettere maiuscola, una minuscola, un numero ed un carattere speciale (._%+-).</p>
        <%
            String matching = (String) session.getAttribute("matching");
            if(matching != null && matching.equalsIgnoreCase("false")){
        %>
        <p style="color: red">La password non rispetta il formato richiesto. Inserisci una nuova password.</p>
        <%
            }
        %>
        <button type="submit">Conferma password</button>
    </form>
</div>

</body>
</html>
