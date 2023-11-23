function selectVideolesson(numberLesson, urlLezione){

    var xmlhttp = new XMLHttpRequest();
    var parametro = "lesson=" + urlLezione;

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            zoomBlock(this, numberLesson);
        }
    };

    xmlhttp.open("POST", "scelta-lezione", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);

}

function zoomBlock(xmlhttp, numBlock) {
    var json = JSON.parse(xmlhttp.responseText);
    var block = document.getElementById("videolesson-block");
    var videoSelect = document.getElementById(numBlock);
    var videos = block.querySelectorAll('img');
    var chat = document.getElementById("chat");
    var button = document.getElementById("button-chat");

    for (var i = 0; i < videos.length; i++) {
        videos[i].setAttribute("style", "");
    }

    videoSelect.setAttribute("style", "max-width: 100%");

    let divMessage = "";

    if (json != null && json.length > 0) {
        for (let i = 0; i < json.length; i++) {
            if(i%2 === 0)
                divMessage += "<div class='message user-message'>" + json[i].userQuery + "</div>";
            else
                divMessage += "<div class='message bot-message'>" + json[i].botResponse + "</div>";
        }
    } else {
        divMessage = "<div class='message bot-message'>Adesso puoi iniziare a fare domande sulla videolezione selezionata! <strong>NOTA BENE:</strong>Le risposte del modello " +
            "non potrebbero sempre essere corrette. Fornisci le domande in modo corretto al fine di ottenere una risposta più " +
            "accurate da parte le modello.</div></div>";
    }

    let funzione = "sendMessage('lesson" + numBlock + "')";
    button.setAttribute('onclick', funzione);
    chat.innerHTML = divMessage
}

function sendMessage(lessonSelected){

    var xmlhttp = new XMLHttpRequest();
    var inputBox = document.getElementById('message-input');
    var query = inputBox.value;
    var parametro = "query=" + query + "&lessonSelected=" + lessonSelected;

    let userMessage = "<div class='message user-message'>" + query + "</div>";
    document.getElementById('chat').insertAdjacentHTML('beforeend', userMessage);

    let loadingImg = "<div id='loadingDiv' class='message bot-message'>" +
        "<img src='./images/loading.gif' height='60' width='60'>" +
        "</div>"
    document.getElementById('chat').insertAdjacentHTML('beforeend', loadingImg);

    inputBox.value = "";

    xmlhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            chatbotMessage(this);
        }
    };

    xmlhttp.open("POST", "chat-servlet", true);
    xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xmlhttp.send(parametro);

}

function chatbotMessage(xmlhttp){
    var json =JSON.parse(xmlhttp.responseText);
    var response = json[0].response_bot;
    var chat = document.getElementById("chat");

    let botMessage = "<div class='message bot-message'>" + response + "</div>";

    document.getElementById('loadingDiv').remove();

    chat.insertAdjacentHTML('beforeend', botMessage);
}
