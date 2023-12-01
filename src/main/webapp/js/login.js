document.addEventListener('DOMContentLoaded', function () {

    // Chiudi il popup quando viene cliccato il pulsante di chiusura o l'area oscura intorno al popup
    document.getElementById('closePopup').addEventListener('click', function () {
        document.getElementById('popup').style.display = 'none';
    });

    document.querySelector('.popup').addEventListener('click', function (event) {
        if (event.target === this) {
            document.getElementById('popup').style.display = 'none';
        }
    });
});