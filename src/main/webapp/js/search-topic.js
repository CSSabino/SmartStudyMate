function search() {
    var searchInput = document.getElementById('search-input').value;
    var xmlhttp = new XMLHttpRequest();
    var parametro = "topic=" + searchInput;

    var divVideo = document.getElementById('videolezione-container');
    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='images/loading.gif' height='60' width='60'>" +
        "<p>Ricerca in corso... <strong>NOTA BENE: </strong> non affidarsi sempre al modello.</p>" +
        "</div>"
    divVideo.innerHTML = loadingImg;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            searchTopic(this);
        }
    };

    xmlhttp.open("POST", "search-topic", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);
}

function searchTopic(xmlhttp){
    var json = JSON.parse(xmlhttp.responseText);
    var divVideo = document.getElementById('videolezione-container');
    var htmlDivVideo = "";

    if (json != null && json.length > 0) {
        htmlDivVideo += "<iframe width='500' height='320' src='" + json[0].url_video + "' frameborder='0' allowfullscreen></iframe>" +
            "<p>" + json[0].summarization + "</p>"
    } else {
        htmlDivVideo = "<p>Il topic inserito non rientra nei contenuti del videocorso. Riprova con un altro topic!</p>";
    }

    divVideo.innerHTML = htmlDivVideo;
}