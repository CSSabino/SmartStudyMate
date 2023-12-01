<%@ page import="com.ssm.smartstudymate.model.Videolezione" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.ssm.smartstudymate.model.Cronologia" %>
<%@ page import="com.ssm.smartstudymate.model.Chat" %>
<%@ page import="com.ssm.smartstudymate.model.Docente" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>SmartStudyMate | HOME</title>
    <link rel="stylesheet" href="./css/home.css" type="text/css">
    <script type="text/javascript" src="./js/home.js" defer></script>
    <script type="text/javascript" src="./js/login.js" defer></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
</head>
<body>

<header>
    <h1>Ciao sono SmartStudyMate, il tuo amico intelligente per lo studio!</h1>
</header>

<a href="./logoutServlet" id="logout"><span>Logout <i class="fa fa-sign-out"></i></span></a>

<nav>
    <a href="#">Home</a>
    <a href="router-servlet?filejsp=search_topic.jsp">Search Topic</a>
    <a href="router-servlet?filejsp=quiz_mate.jsp">Quiz Mate</a>
</nav>

<%
    ArrayList<Videolezione> playlist = (ArrayList<Videolezione>) session.getAttribute("videolezioni");
    Videolezione videolezioneSelezionata = null;
    String accessCode = (String) request.getAttribute("access-code");
    if(accessCode != null){
%>
<div class="popup" id="popup">
    <div class="popup-content">
        <span class="close" id="closePopup">&times;</span>
        <h2>CODICE CREATO CORRETTAMENTE</h2>
        <p>Copialo e forniscilo ai tuoi studente per consentire loro l'accesso a <strong>SmartStudyMate</strong></p>
        <input type="text" id="myInput" value="<%=accessCode%>" readonly>
        <button id="copy-button" onclick="copyText()">Copia testo</button>
    </div>
</div>
<%
    }
%>
<div class="container">
    <div class="left-block">
        <!-- Blocco con immagini e scrollbar a destra -->
        <div id="videolesson-block" class="image-container">
            <%
                Docente docente = (Docente) session.getAttribute("utente");
                if(docente != null){
            %>
            <div>
                <a href="router-servlet?filejsp=add_lesson.jsp" id="add_lesson">Aggiungi videolezione</a>
                <%
                    if(!playlist.isEmpty()){
                %>
                    <a href="./genera-codice" id="gen_code">Genera codice</a>
                <%
                    }
                %>

            </div>
            <br><br>
            <%
                }
            %>
            <%
                // URL della videolezione selezionata
                String lessonSelected = (String) session.getAttribute("lesson-selected");

                if(playlist != null){

                    for(int i = 0; i < playlist.size(); i++){
                        Videolezione videolezione = playlist.get(i);
                        if(videolezione.getUrlVideo().equalsIgnoreCase(lessonSelected)){
                            videolezioneSelezionata = videolezione;
            %>
            <div id="image-item<%=i%>" class="image-item">
                <img id="<%=i%>" src="<%=videolezione.getUrlPhotoVideo()%>" alt="<%=videolezione.getTitolo()%>" onclick="zoom(this.id); selectVideolesson(this.id, '<%=videolezione.getUrlVideo()%>')" style="max-width: 100%; border: 3px solid green">
                <span><strong><%=videolezione.getTitolo()%></strong></span>
            </div>

            <%
            } else {
            %>

            <div id="image-item<%=i%>" class="image-item">
                <img id="<%=i%>" src="<%=videolezione.getUrlPhotoVideo()%>" alt="<%=videolezione.getTitolo()%>" onclick="zoom(this.id); selectVideolesson(this.id, '<%=videolezione.getUrlVideo()%>')">
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
            <h1>Chatbot</h1>
        </div>
        <div class="chat-container">
            <div id="chat" class="chat-messages">
                <%
                    Cronologia cronologia = (Cronologia) session.getAttribute("cronologia");
                    Chat chat = null;

                    if(lessonSelected != null){
                        chat = cronologia.recuperaChat(lessonSelected);
                    }

                    if(chat == null){
                %>

                <div class="message bot-message">Scegli una videolezione prima di poter chattare con me! <strong>NOTA BENE:</strong>Le risposte del modello
                    non potrebbero sempre essere corrette. Fornisci le domande in modo corretto al fine di ottenere risposte più
                accurate da parte del modello.</div>

                <%
                } else {
                    ArrayList<String> conversation = chat.leggiMessaggi();

                    if(!conversation.isEmpty()){

                        int i = 0;

                        for(String message : conversation){
                            if(i%2!=0){

                %>
                <div class="message bot-message"><%=message%></div>

                <%
                } else {
                %>
                <div class="message user-message"><%=message%></div>
                <%
                        }
                        i++;
                    }
                } else {

                %>

                <div class="message bot-message">Inizia a fare domande sulla lezione "<%=videolezioneSelezionata.getTitolo()%>".
                    <strong>NOTA BENE: </strong> Le risposte del modello potrebbero NON ESSERE SEMPRE corrette. Fornisci le domande in modo corretto al fine di ottenere una risposta più
                    accurata da parte le modello.</div>

                <%
                        }
                    }
                %>
            </div>
        </div>

        <div id="message-form">

            <input type="text" id="message-input" placeholder="Cosa dice il professore in merito a..." required>
            <%
                if(lessonSelected != null){
            %>
            <button id="button-chat" type="submit" onclick="sendMessage('<%=lessonSelected%>')">Invia</button>
            <%
                } else {
            %>
            <button id="button-chat" type="submit" disabled>Invia</button>
            <%
                }
            %>

        </div>

    </div>

</div>

</body>
</html>
