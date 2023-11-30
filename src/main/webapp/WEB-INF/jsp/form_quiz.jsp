<%@ page import="com.ssm.smartstudymate.model.Videolezione" %>
<%@ page import="com.ssm.smartstudymate.model.VideolezioneDAO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartStudyMate</title>
    <link rel="stylesheet" href="./css/login.css" type="text/css">
    <script type="text/javascript" src="./js/form_quiz.js" defer></script>
</head>
<body>

<form action='router-servlet?filejsp=quiz_mate.jsp' method='post'>
    <button type='submit'>TORNA INDIETRO</button>
    </form>

<%
    String lessonSelected = (String) session.getAttribute("lesson-selected");
    Videolezione videolezione = new Videolezione();

    if(lessonSelected != null){
        VideolezioneDAO videolezioneDAO = new VideolezioneDAO();
        videolezione = videolezioneDAO.doRetrieveByUrl(lessonSelected);
    }
%>
<div id="quizForm">

    <%
        String quizGenerale = (String) session.getAttribute("quiz_generale");
        if(quizGenerale != null && quizGenerale.equalsIgnoreCase("true")){
    %>
        <h2>Come deve essere composto il tuo quiz?</h2>
    <%
        } else {
    %>
    <h2>Come deve essere composto il tuo quiz sulla lezione "<%=videolezione.getTitolo()%>"?</h2>
    <%
        }
    %>
    <div id="contentQuiz">
        <form action="#" method="post">
            <br>
            <label class="quiz-label" for="multiple_choice">Scelta multipla:</label>
            <input class="quiz-input" type="number" id="multiple_choice" name="multiple_choice" min="0" max="10" value="0" required>

            <label class="quiz-label" for="true_false">Vero/Falso:</label>
            <input class="quiz-input" type="number" id="true_false" name="true_false" min="0" max="10" value="0" required>
            <br>
            <label class="quiz-label" for="matching">Matching:</label>
            <input class="quiz-input" type="number" id="matching" name="matching" min="0" max="10" value="0" required>

            <label class="quiz-label" for="short_answer">Short Answer:</label>
            <input class="quiz-input" type="number" id="short_answer" name="short_answer" min="0" max="10" value="0" required>
            <br>
            <label class="quiz-label" for="numerical">Numerica:</label>
            <input class="quiz-input" type="number" id="numerical" name="numerical" min="0" max="10" value="0" required>

            <label class="quiz-label" for="essay">Essay:</label>
            <input class="quiz-input" type="number" id="essay" name="essay" min="0" max="10" value="0" required>
            <br><br>
            <%
                if(quizGenerale != null && quizGenerale.equalsIgnoreCase("true")){
            %>
                <button id="crea-file" type="button" onclick="creaQuizMoodleAiken('generale')">Crea il file scaricabile del tuo quiz</button>
            <%
                } else {
                    String titolo = videolezione.getTitolo().replace("'", "\\'");
            %>
                <button id="crea-file" type="button" onclick="creaQuizMoodleAiken('<%=titolo%>')">Crea il file scaricabile del tuo quiz</button>
            <%
                }
            %>
        </form>
    </div>
    <div id="div-download">
        <%
            String downloadDisponibile = (String) session.getAttribute("download-disponibile");
            if(downloadDisponibile != null && downloadDisponibile.equalsIgnoreCase("true")){
                String moodle = (String) session.getAttribute("quizmoodle");
                String aiken = (String) session.getAttribute("quizaiken");
        %>
        <p><strong>Scegli il formato desiderato e scarica il tuo quiz!</strong></p>
        <%
            if(moodle.equalsIgnoreCase("true")){
        %>
        <button id="crea-moodle" type="button" onclick="downloadFile('moodle.xml')">MOODLE XML</button>
        <%
            }
            if(aiken.equalsIgnoreCase("true")){
        %>
        <button id="crea-aiken" type="button" onclick="downloadFile('aiken.txt')">AIKEN</button>
        <%
                }
            }
        %>
    </div>
</div>

</body>
</html>
