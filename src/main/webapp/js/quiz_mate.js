function selectVideolesson(numberLesson, urlLezione, titoloLezione){

    var xmlhttp = new XMLHttpRequest();
    var parametro = "lesson=" + urlLezione;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            zoomBlock(this, numberLesson, titoloLezione);
        }
    };

    xmlhttp.open("POST", "scelta-lezione", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);

}

function zoomBlock(xmlhttp, numBlock, titoloLezione) {
    var block = document.getElementById("videolesson-block");
    var videoSelect = document.getElementById(numBlock);
    var videos = block.querySelectorAll('img');
    var chat = document.getElementById("quiz-container");
    var divDocenteQuiz = document.getElementById("crea_quiz_docente");

    document.getElementById("quiz-button").removeAttribute("disabled");

    let htmlDivDocenteQuiz = "<p>" +
                            "<form action='router-servlet?filejsp=form_quiz.jsp' method='post'>" +
                            "<button id='crea_quiz' name='quiz_button' type='submit'>Crea quiz sulla lezione \"" + titoloLezione + "\"</button>" +
                            "</form>" +
                            "</p>"


    for (var i = 0; i < videos.length; i++) {
        videos[i].setAttribute("style", "");
    }

    videoSelect.setAttribute("style", "max-width: 100%");

    let divMessage = "";

    divMessage = "<div class='message bot-message'>Adesso puoi creare quiz sulla videolezione selezionata! <br><strong>NOTA BENE: </strong>Le risposte del modello " +
            "non potrebbero sempre essere corrette.</div>";

    chat.innerHTML = divMessage
    divDocenteQuiz.innerHTML = htmlDivDocenteQuiz;
}

function valutaRisposta() {
    var xmlhttp = new XMLHttpRequest();
    var domanda = document.getElementById("domanda").textContent;
    var rispostaUtente = document.querySelector('input[name="risposta"]:checked').value;

    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<h3>Valutazione del quiz in corso...<br> <strong>NOTA BENE: </strong>Le risposte del modello " +
        "non potrebbero sempre essere corrette.</div><h3>" +
        "</div>"
    document.getElementById('quiz-container').innerHTML = loadingImg;

    var parametri = "domanda="+domanda+"&risposta="+rispostaUtente;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            valutaRipostaQuiz(this, domanda, rispostaUtente);
        }
    };

    xmlhttp.open("POST", "valuta-quiz", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametri);

}

function valutaRipostaQuiz(xmlhttp, domanda, risposta){
    var json = JSON.parse(xmlhttp.responseText);
    let htmlValutazione = "";

    if(json != null && json.length > 0){
        let valutazione = json[0].valutazione;
        if(valutazione.toLowerCase() === "corretta"){
            htmlValutazione += "<div id='divValutazione'>" +
                "<p>Complimenti! La risposta \"" + risposta + "\" alla domanda \"" + domanda + "\" è corretta!<br>" +
                "<strong>Genera un nuovo quiz!</strong></p>" +
                "</div>";
        } else {
            htmlValutazione += "<div id='divValutazione'>" +
                "<p>La risposta \"" + risposta + "\" alla domanda \"" + domanda + "\" non è corretta. Puoi generare " +
                "un nuovo quiz oppure rivedere la lezione che parla dell'argomento della domanda" +
                "</p>" +
                "<form id='myform' action='search-topic' method='post'>" +
                "<input type='hidden' name='topic' value='"+domanda+"'>" +
                "<input type='hidden' name='ripetizione' value='true'>" +
                "<button id='rivedilezioneButton' type='submit'>RIPETI L'ARGOMENTO</button> " +
                "</form> " +
                "</div>";
        }
    }

    console.log(htmlValutazione);
    document.getElementById("quiz-container").innerHTML = htmlValutazione;
    document.getElementById("value-button").setAttribute("disabled", true);
}

function creaQuiz(){

    var xmlhttp = new XMLHttpRequest();

    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<h3>Creazione del quiz in corso...<br><strong>NOTA BENE: </strong>Le risposte del modello " +
        "non potrebbero sempre essere corrette.</div><h3>" +
        "</div>"
    document.getElementById('quiz-container').innerHTML = loadingImg;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            requestQuiz(this);
        }
    };

    xmlhttp.open("POST", "quiz-servlet", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send();
}

function requestQuiz(xmlhttp){

    var json = JSON.parse(xmlhttp.responseText);
    var divQuiz = document.getElementById('quiz-container');
    var htmlDivQuiz = "";

    if (json != null && json.length > 0) {
        htmlDivQuiz = json[0].response_bot;
    } else {
        htmlDivQuiz = "<p><strong>ERRORE NELLE CREAZIONE DEL QUIZ!</strong></p>";
    }

    divQuiz.innerHTML = htmlDivQuiz;
    document.getElementById("value-button").removeAttribute("disabled");
}

function toSearchTopicJsp(){
    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<h3>Reindirizzamento al motore di ricerca per contenuto...<br> <strong>NOTA BENE: </strong>Le risposte del modello " +
        "non potrebbero sempre essere corrette.</div><h3>" +
        "</div>"
    document.getElementById('quiz-container').innerHTML = loadingImg;
}