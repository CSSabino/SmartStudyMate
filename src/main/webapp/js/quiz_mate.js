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
                            "<form action='quizmoodleaiken-servlet' method='post'>" +
                            "<button id='crea_quiz' name='quiz_button' type='submit'>Crea quiz sulla lezione \"" + titoloLezione + "\"</button>" +
                            "</form>" +
                            "</p>"


    for (var i = 0; i < videos.length; i++) {
        videos[i].setAttribute("style", "");
    }

    videoSelect.setAttribute("style", "max-width: 100%");

    let divMessage = "";

    divMessage = "<div class='message bot-message'>Adesso puoi creare quiz sulla videolezione selezionata! <strong>NOTA BENE:</strong>Le risposte del modello " +
            "non potrebbero sempre essere corrette.</div>";

    chat.innerHTML = divMessage
    divDocenteQuiz.innerHTML = htmlDivDocenteQuiz;
}

function valutaRisposta() {
    var domanda = document.getElementById("domanda");
    var rispostaUtente = document.querySelector('input[name="risposta"]:checked');

    console.log(domanda.textContent + "////" + rispostaUtente.value);

}

function creaQuiz(){

    var xmlhttp = new XMLHttpRequest();

    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<h3>Creazione del quiz in corso...<h3>" +
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

