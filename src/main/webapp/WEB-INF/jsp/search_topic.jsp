<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartStudyMate | SEARCH TOPIC</title>
    <link rel="stylesheet" href="./css/home.css" type="text/css">
    <script type="text/javascript" src="./js/search-topic.js" defer></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon">
</head>

<body>

<header>
    <h1>Motore di ricerca per contenuto</h1>
</header>

<a href="./logoutServlet" id="logout"><span>Logout <i class="fa fa-sign-out"></i></span></a>

<nav>
    <a href="router-servlet?filejsp=home.jsp">Home</a>
    <a href="#">Search Topic</a>
    <a href="router-servlet?filejsp=quiz_mate.jsp">Quiz Mate</a>
</nav>

<div class="search-container">
    <input type="text" id="search-input" placeholder="Cerca..." required>
    <button id="search-button" onclick="search()">Cerca</button>
</div>

<div id="videolezione-container">

    <%
        String searchDone = (String) session.getAttribute("search_done");

        if(searchDone != null && searchDone.equalsIgnoreCase("true")){
            String urlVideoEmbeded = (String) session.getAttribute("url_video_embeded");
            String summary = (String) session.getAttribute("summary");
    %>

    <iframe width="500" height="320" src="<%=urlVideoEmbeded%>" frameborder="0" allowfullscreen></iframe>
    <p> <%=summary%> </p>

    <%
        } else {
    %>
        <p>
            <h2>
                Fornisci attraverso la barra di ricerca l'argomento che ti interessa.<br>In questo spazio verrà mostrata la videolezione
                a partire dal minuto nel quale inizia la spiegazione dell'argomento che ti interessa. <strong>NOTA BENE: </strong>Le risposte del modello
                non potrebbero sempre essere corrette.
            </h2>
        </p>
    <%
        }
    %>
</div>

</body>
</html>
