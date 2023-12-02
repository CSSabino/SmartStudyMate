<%@ page import="com.ssm.smartstudymate.model.Videolezione" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.ssm.smartstudymate.model.Docente" %>
<%@ page import="com.ssm.smartstudymate.model.VideolezioneDAO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>SmartStudyMate | QUIZ MATE</title>
    <link rel="stylesheet" href="./css/home.css" type="text/css">
    <script type="text/javascript" src="./js/quiz_mate.js" defer></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon">
</head>
<body>

<header>
    <h1>Rispondi al quiz e valuta il tuo apprendimento!</h1>
</header>

<a href="./logoutServlet" id="logout"><span>Logout <i class="fa fa-sign-out"></i></span></a>

<nav>
    <a href="router-servlet?filejsp=home.jsp">Home</a>
    <a href="router-servlet?filejsp=search_topic.jsp">Search Topic</a>
    <a href="#">Quiz Mate</a>
</nav>

<div class="container">
    <div class="left-block">
        <div id="videolesson-block" class="image-container">
            <div>
                <br>
                <%
                    ArrayList<Videolezione> playlist = (ArrayList<Videolezione>) session.getAttribute("videolezioni");
                    Docente docente = (Docente) session.getAttribute("utente");
                    Videolezione videolezioneSelezionata = null;
                    // URL della videolezione selezionata
                    String lessonSelected = (String) session.getAttribute("lesson-selected");
                    String codeForQuiz = (String) session.getAttribute("codeForQuiz");

                    if(playlist != null){
                        if(docente != null){
                %>

                <form action="router-servlet?filejsp=form_quiz.jsp" method="post">
                    <button id="crea_general_quiz" name="quiz_general_button" type="submit">Crea quiz sulle videolezioni</button>
                    <input type="hidden" name="quiz_generale" value="true">
                </form>
                <br>
                <div id="crea_quiz_docente">
                    <%
                        if(lessonSelected != null){
                            VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
                            Videolezione vd = videolezioneDAO.doRetrieveByUrl(lessonSelected);
                    %>


                    <form action="router-servlet?filejsp=form_quiz.jsp" method="post">
                        <button id="crea_quiz" name="quiz_button" type="submit">Crea quiz sulla lezione "<%=vd.getTitolo()%>"</button>
                    </form>
                    <br>

                    <%
                        }
                    %>
                </div>
                <%
                    }
                %>


            </div>
            <%
                for(int i = 0; i < playlist.size(); i++){
                    Videolezione videolezione = playlist.get(i);
                    String titolo = videolezione.getTitolo().replace("'", "\\'");
                    if(videolezione.getUrlVideo().equalsIgnoreCase(lessonSelected)){
                        videolezioneSelezionata = videolezione;

            %>
            <div id="image-item<%=i%>" class="image-item">
                <img id="<%=i%>" src="<%=videolezione.getUrlPhotoVideo()%>" alt="<%=titolo%>" onclick="zoom(this.id), selectVideolesson(this.id, '<%=videolezione.getUrlVideo()%>', '<%=titolo%>')" style="max-width: 100%; border: 3px solid red">
                <span><strong><%=videolezione.getTitolo()%></strong></span>
            </div>

            <%
            } else {
            %>

            <div id="image-item<%=i%>" class="image-item">
                <img id="<%=i%>" src="<%=videolezione.getUrlPhotoVideo()%>" alt="<%=videolezione.getTitolo()%>"  onclick="zoom(this.id), selectVideolesson(this.id, '<%=videolezione.getUrlVideo()%>', '<%=titolo%>')">
                <span><strong><%=videolezione.getTitolo()%></strong></span>
            </div>

            <%
                        }
                    }
                }
            %>
        </div>
    </div>

    <div id="right-block" class="right-block">

        <div id="intestazione-chat">
            <h1>Rispondi al quiz</h1>
        </div>
        <div id="quiz-container" class="chat-container">

            <%
                if(codeForQuiz != null){
            %>
            <%=codeForQuiz%>
            <%
            } else {
            %>
            <div class="message bot-message">Scegli una videolezione prima di poter rispondere al quiz. <strong>NOTA BENE: </strong>Le risposte del modello
                non potrebbero sempre essere corrette.</div>
            <%
                }
            %>
        </div>
        <div id="value-button-box">
            <%
                if(videolezioneSelezionata == null){
            %>
            <button id="quiz-button" type="submit" onclick="creaQuiz()" disabled>Crea un quiz</button>
            <%
            } else {
            %>
            <button id="quiz-button" type="submit" onclick="creaQuiz()">Crea un quiz</button>
            <%
                }

                if(codeForQuiz == null){
            %>
            <button id="value-button" type="submit" onclick="valutaRisposta()" disabled>Valuta la tua risposta</button>
            <%
            } else {
            %>
            <button id="value-button" type="submit" onclick="valutaRisposta()">Valuta la tua risposta</button>
            <%
                }
            %>
        </div>
    </div>
</div>

</body>
</html>