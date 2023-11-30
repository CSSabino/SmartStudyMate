function downloadFile(file) {
    // Crea un elemento di ancoraggio dinamicamente
    var anchor = document.createElement("a");
    anchor.style.display = "none";
    document.body.appendChild(anchor);

    // Imposta l'URL della servlet che gestisce il download
    var downloadServletUrl = "download-servlet";

    // Aggiungi eventuali parametri necessari all'URL
    downloadServletUrl += "?file=" + file;

    // Imposta l'attributo "href" dell'elemento di ancoraggio con l'URL della servlet
    anchor.href = downloadServletUrl;

    // Imposta l'attributo "download" per specificare il nome del file da scaricare
    anchor.download = "empty.xml"; // Sostituisci con il nome del tuo file

    // Simula un clic sull'elemento di ancoraggio per avviare il download
    anchor.click();

    // Rimuovi l'elemento di ancoraggio dopo il download
    document.body.removeChild(anchor);
}

function creaQuizMoodleAiken(titoloLezione){
    var xmlhttp = new XMLHttpRequest();
    var multiple = document.getElementById("multiple_choice").value;
    var trueFalse = document.getElementById("true_false").value;
    var matching = document.getElementById("matching").value;
    var shortAnswer = document.getElementById("short_answer").value;
    var numerical = document.getElementById("numerical").value;
    var essay = document.getElementById("essay").value;
    var parametro = "multiple_choice=" + multiple + "&true_false=" + trueFalse + "&matching=" + matching +
        "&short_answer=" + shortAnswer + "&numerical=" + numerical + "&essay=" + essay + "&tipoQuiz=" + titoloLezione;

    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<h3>Creazione del quiz in corso...<br><strong>NOTA BENE: </strong>Le risposte del modello " +
        "non potrebbero sempre essere corrette.</div><h3>" +
        "</div>"

    document.getElementById('div-download').innerHTML = loadingImg;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            generateQuiz(this);
        }
    };

    xmlhttp.open("POST", "quizmoodleaiken-servlet", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);
}

function generateQuiz(xmlhttp){
    var json = JSON.parse(xmlhttp.responseText);
    let htmlDownload = "<p><strong>Scegli il formato desiderato e scarica il tuo quiz!</strong></p>";

    if(json != null && json.length > 0){
        let moodle = json[0].quiz_moodle;
        let aiken = json[0].quiz_aiken;
        if(moodle === "true"){
            htmlDownload += "<button id=\"crea-moodle\" type=\"button\" onclick=\"downloadFile('moodle.xml')\">MOODLE XML</button>";
        }
        if(aiken === "true"){
            htmlDownload += "<button id=\"crea-aiken\" type=\"button\" onclick=\"downloadFile('aiken.txt')\">AIKEN</button>";
        }
    }

    document.getElementById("div-download").innerHTML = htmlDownload;

}