function selectVideolesson(numberLesson, urlLezione){

    var xmlhttp = new XMLHttpRequest();
    var parametro = "lesson=" + urlLezione;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            zoomBlock(this, numberLesson, urlLezione);
        }
    };

    xmlhttp.open("POST", "scelta-lezione", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);

}

function zoomBlock(xmlhttp, numBlock, urlLezione) {
    var json = JSON.parse(xmlhttp.responseText);
    var block = document.getElementById("videolesson-block");
    var videoSelect = document.getElementById(numBlock);
    var videos = block.querySelectorAll('img');
    var buttons = block.querySelectorAll('button');
    var chat = document.getElementById("chat");
    var button = document.getElementById("button-chat");
    var imageBlockButton = document.getElementById("button" + numBlock)

    for (var i = 0; i < videos.length; i++) {
        videos[i].setAttribute("style", "");
    }

    for (var i = 0; i < buttons.length; i++) {
        buttons[i].setAttribute("style", "display: none");
    }

    videoSelect.setAttribute("style", "max-width: 100%");
    imageBlockButton.setAttribute("style", "")

    let divMessage = "";

    if (json != null && json.length > 0) {
        for (let i = 0; i < json.length; i++) {
            if(i%2 === 0)
                divMessage += "<div class='message user-message'>" + json[i].userQuery + "</div>";
            else
                divMessage += "<div class='message bot-message'>" + json[i].botResponse + "</div>";
        }
    } else {
        divMessage = "<div class='message bot-message'>Adesso puoi iniziare a fare domande sulla videolezione selezionata!</div>";
    }

    let funzione = "sendMessage('lesson" + numBlock + "')";
    button.setAttribute('onclick', funzione);
    chat.innerHTML = divMessage
}