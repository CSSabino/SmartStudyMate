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
    anchor.download = file; // Sostituisci con il nome del tuo file

    // Simula un clic sull'elemento di ancoraggio per avviare il download
    anchor.click();

    // Rimuovi l'elemento di ancoraggio dopo il download
    document.body.removeChild(anchor);
}