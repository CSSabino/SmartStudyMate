from flask import Flask, request
from googleapiclient.discovery import build
from langchain.document_loaders import YoutubeLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores import FAISS
from langchain.chat_models import ChatOpenAI
from langchain.prompts.chat import (ChatPromptTemplate, SystemMessagePromptTemplate, HumanMessagePromptTemplate)
from langchain.chains import LLMChain
import os
import random
import re
from urllib.parse import urlparse, parse_qs

# Chiave API di OpenAI
os.environ["OPENAI_API_KEY"] = "personal_openai_api_key"

embeddings = OpenAIEmbeddings()
app = Flask(__name__)

# Chiave API di YouTube
api_key = "personal_youtube_api_key"

# Connessione all'API di YouTube
youtube = build('youtube', 'v3', developerKey=api_key)


# RECUPERA L'ID DEL VIDEO DALL'URL DEL VIDEO
def get_video_id_from_url(youtube_url):
    query = urlparse(youtube_url)
    if query.hostname == 'www.youtube.com' or query.hostname == 'youtube.com':
        if 'v' in query.query:
            return parse_qs(query.query)['v'][0]
        else:
            path = query.path.split('/')
            return path[-1]
    elif query.hostname == 'youtu.be':
        return query.path[1:]
    else:
        return None


# RECUPERA L'URL DELLA FOTO DELLA VIDEOLEZIONE IN BASE ALL'ID DEL VIDEO
def get_video_info(video_id):
    # Esegui la richiesta per ottenere le informazioni sul video
    request = youtube.videos().list(part='snippet,statistics', id=video_id)
    response = request.execute()

    # Estrai le informazioni desiderate
    if response['items']:
        video = response['items'][0]
        thumbnail_url = video['snippet']['thumbnails']['high']['url']  # URL dell'immagine di copertina
    else:
        thumbnail_url = ""

    return thumbnail_url


# CREAZIONE DATABASE VETTORIZZATO
def create_db_from_youtube_video_url(video_url):
    loader = YoutubeLoader.from_youtube_url(video_url, language=["it"])
    transcript = loader.load()
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=10000, chunk_overlap=2200)
    docs = text_splitter.split_documents(transcript)
    app.config['all_docs'] = docs
    db = FAISS.from_documents(docs, embeddings)
    app.config['database'] = db
    return db


# RICERCA DI SIMILARITÀ
def get_similarity_from_query(db, query, k=2):
    docs = db.similarity_search(query, k=k)
    docs_page_content = " ".join([d.page_content for d in docs])
    return docs, docs_page_content


# RISPOSTA ALLA QUERY RISPETTO ALLA VIDEOLEZIONE (chatbot)
def get_response_from_query(db, query):
    docs, docs_page_content = get_similarity_from_query(db, query)
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.5)
    template = """Ciao ChatGPT, tu in questo momento sei un helpfull assistent che risponde a delle domande fornite 
                    da utenti in base alla trascrizione del video: {docs}

                Utilizza solo le informazioni della trascrizione per rispondere alla domanda.

                Se ritieni di non avere abbastanza informazioni per rispondere alla domanda, dì "Non lo so".

                Le tue risposte dovrebbero essere dettagliate ed esaustive."""

    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "{question}\nTi ricordo che devi rispondere solo sulla base delle informazioni riportate " \
                     "dall'insegnante che sono contenute nella trascrizione del video."
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )
    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(question=query, docs=docs_page_content)

    response = response.replace("\n", "")

    return response


# CREA IL QUIZ PER L'UTENTE
def quiz_creator():
    db = app.config['all_docs']
    docs = [random.choice(db) for _ in range(2)]
    docs_page_content = " ".join([d.page_content for d in docs])

    topic = retrieveTopic(docs_page_content)

    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.5)
    template = """Tu sei un creatore di quiz. Forniscimi un quiz composto da una sola domanda su argomenti dati 
    dall'utente. La tua risposta deve contenere solo il codice HTML con il tag <div> e l'attributo 'name" delle 
    riposte deve essere "risposta"."""

    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "Trascrizione: {transcript}. Formula l'output solo in base alle informazioni della " \
                     "trascrizione.\n\nINPUT: HTML\nOUTPUT: {example}\nINPUT:{topics}\nOUTPUT:"
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )
    html_content = '''
    <div class="quiz_div">
      <h2>Domanda sulle Servlet</h2>
      <p id="domanda">Cosa rappresenta una Servlet in Java?</p>

      <label>
        <input id="opzione1" type="radio" name="risposta" value="Un framework JavaScript"> 
        Un framework JavaScript
      </label>
      <br>
      <label>
        <input id="opzione2" checked type="radio" name="risposta" value="Un'applicazione web"> 
        Un'applicazione web
      </label>
      <br>
      <label>
        <input id="opzione3" type="radio" name="risposta" value="Un componente Java per la gestione delle richieste HTTP"> 
        Un componente Java per la gestione delle richieste HTTP
      </label>
      <br>
      <label>
        <input id="opzione4" type="radio" name="risposta" value="Una libreria CSS"> 
        Una libreria CSS
      </label>
    </div>
    '''

    print("Siamo prima di LLMChain()...\n")
    chain = LLMChain(llm=chat, prompt=chat_prompt)

    print("Stiamo creando la risposta...\n")
    response = chain.run(transcript=docs_page_content, example=html_content, topics=topic)

    return response


# RESTITUISCE UN ARGOMENTO TRATTATO ALL'INTERNO DI UN TESTO PASSATO PER ARGOMENTO
def retrieveTopic(docs_page_content):
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.2)
    template = """Tu sei un analizzatore di testi ed il tuo compito è fornire un titolo per il testo fornito. Segui 
    l'esempio:"""
    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "{example}\n\nTESTO2: {transcript}\nTITOLO TESTO2:"
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )
    esempio_summary = '''TESTO1: La Seconda Guerra Mondiale, che ebbe luogo tra il 1939 e il 1945, è stato uno dei conflitti globali più devastanti e significativi della storia umana. Il conflitto coinvolse la stragrande maggioranza delle nazioni mondiali, divise in due alleanze principali: gli Alleati, guidati da Stati Uniti, Unione Sovietica, Regno Unito e altri paesi, e le Potenze dell'Asse, guidate da Germania, Italia e Giappone.
    Le radici della Seconda Guerra Mondiale affondano nelle tensioni e negli sconvolgimenti derivanti dalla Prima Guerra Mondiale. La situazione economica instabile, i trattati di pace insoddisfacenti e le crescenti ambizioni di alcuni leader politici crearono un terreno fertile per l'ascesa di regimi totalitari, in particolare il nazismo in Germania sotto il controllo di Adolf Hitler.
    Il conflitto ebbe inizio il 1 settembre 1939, quando la Germania invase la Polonia. In risposta, Regno Unito e Francia dichiararono guerra alla Germania. Nel corso degli anni successivi, il conflitto si estese a nuove regioni, coinvolgendo gran parte dell'Europa, dell'Africa, dell'Asia e del Pacifico.
    Una delle caratteristiche distintive della Seconda Guerra Mondiale fu l'uso estensivo di nuove tecnologie militari, inclusi aerei, carri armati, sottomarini e armi atomiche. La guerra terrestre, aerea e navale raggiunse proporzioni senza precedenti, causando devastazione e perdite umane su una scala spaventosa.
    Il conflitto vide anche l'olocausto, in cui sei milioni di ebrei furono sistematicamente perseguitati e uccisi dal regime nazista. Questo oscuro capitolo della storia umana rappresenta uno degli episodi più tragici e spaventosi della Seconda Guerra Mondiale.
    Gli eventi cruciali includono la Battaglia di Normandia nel 1944, che portò alla liberazione dell'Europa occidentale, e la Battaglia di Stalingrado nel 1942-1943, che fu una svolta decisiva sulla fronte orientale. Nel Pacifico, la Battaglia di Midway e la Battaglia di Guadalcanal furono decisive per l'equilibrio delle forze nella regione.
    La guerra giunse a una conclusione nel 1945, dopo la resa incondizionata della Germania nel maggio di quell'anno. Tuttavia, il conflitto continuò nel Pacifico fino a quando gli Stati Uniti sganciarono due bombe atomiche su Hiroshima e Nagasaki, portando alla resa del Giappone nel settembre 1945.
    La Seconda Guerra Mondiale ebbe profonde conseguenze politiche, economiche e sociali. Contribuì alla formazione delle Nazioni Unite, un'organizzazione internazionale creata per prevenire conflitti futuri. Inoltre, la guerra portò a importanti cambiamenti nella mappa geopolitica mondiale, con l'emergere degli Stati Uniti e dell'Unione Sovietica come superpotenze. I suoi effetti si riflettono ancora oggi nella memoria collettiva e nei rapporti internazionali.
    TITOLO TESTO1: Seconda Guerra Mondiale'''

    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(example=esempio_summary, transcript=docs_page_content)

    return response


# VALUTA LA RISPOSTA DATA AL QUIZ BASANDOSI SULLA TRASCRIZIONE
def valutaRispostaQuiz(db, domanda, opzione1, opzione2, opzione3, opzione4, risposta):
    docs, docs_page_content = get_similarity_from_query(db, domanda)
    print("Processo di valutazione iniziato...")
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.5)
    template = """Tu sei un valutatore di risposte a domande. 
    Per ogni domanda devi valutare se la risposta è corretta sulla base delle informazioni della trascrizione. 
    Rispondi solo con la valutazione.\n\nESEMPIO:\n{esempio}"""

    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "Trascrizione: {transcript}\n" \
                     "Domanda: {domanda}\n" \
                     "Opzione1: {opzione1}\n" \
                     "Opzione2: {opzione2}\n" \
                     "Opzione3: {opzione3}\n" \
                     "Opzione4: {opzione4}\n" \
                     "Risposta: {risposta}\n" \
                     "Valutazione:"
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )

    esempio = '''Trascrizione: vado in flash che manderò un errore 500 per noi è importante riconoscere questi codici 
    perché se state scrivendo una serie che mi viene da con un errore 400 è inutile che cercate nel codice della 
    serie c'è qualcosa che non va nella richiesta per avere un 400 se avete un 500 c'è un problema nella vostra serve 
    allora quali sono i metodi per lavorare sui campi eder della risposta cioè se tender tu mi dai un nome di un eder 
    e mi dai un valore e io lo sento ad esempio potrei ritardi 7 eder content type poi come paolo zito tex html il 
    mio posso imporre io potrei in forse vedete prendiamo questo messaggio vedete che qui c'è un html giustamente 
    perché concentrare è dato da tex html se se il contenta se la mia serp lett oltre a riempire il corpo della 
    risposta facesse 17 ter content type text plain invece di html sapete cosa succede mago tra yen lato client 
    arriverebbe il messaggio di risposta fondamentale text plain e poi questo codice html secondo voi il browser 
    interpreterà al html no il drive per il browser vi farà vedere il sorgente anche al pianella perché gli avrebbe 
    detto lei mi ha preferito tex e quindi non lo interpreto quindi poi con 7 ter voi potete accettare qualsiasi rete 
    ricordatevi che qui però dovete mettere sempre una stringa con 7 tel se lei del suo a un campo intero ha un 
    valore intero ha un valore che è una data allora voi potete usare set dei crookers se invece ha un valore che è 
    un intero potete usare 7 eder quindi invece se usate questo dovete tradurre la data inserita el intero e 
    distribuita se invece usate setting eder potete utilizzare questo fatto niente hanno un campo che non è una 
    stringa come questo ma hanno un intero tipo con del mio mac quindi voi potete usare set pin ed e poi se volete 
    aggiungere un altro eder potete usare anche del food day and therefore antin eder e gli stessi parametri che vede 
    diffuso posso più fare contenta la lunghezza del contenuto posso aggiungere un tutti poi vedremo che questa cosa 
    qui la utilizzeremo quindi posso aggiungere un tour che hanno risposto oppure posso dire nella risposta che non 
    voglio che tu rimanga un'altra pagina che faccio un sempre diverso se voglio scrivere nel corpo avete visto già 
    che noi utilizziamo il trend writer il solito questo lo si usa per i testi se invece di un testo vogliono inviare 
    un'immagine conviene utilizzare invece andare a prendere un servet output lo stream e scrivere su di esso per un 
    immagine per tutti gli elementi per implementare correttamente il metodo soggetto di euro sampler allora questo 
    adesso invece riguarda un piccolo utilizzo quindi di alcuni di questi metodi potete c'è tex html e qui c'è la 
    scrittura sul field ranica perché è un testo se fosse stato un immagine avrei potuto utilizzare un server south 
    stream per karin evidente se scrivo questa serve ottengo questo messaggio http non ho toccato quest'anno scorso 
    che non c'è scritto però ho toccato fontaine type e riposi andando avanti se io voglio prenderla dinamica ok 
    questo vogliamo sparite io posso leggere i parametri da una richiesta degli adesso qui smettiamo di parlare della 
    risposta parliamo della richiesta quindi che significa che per avere una pagina dinamica di solito o il flyer 
    deve mandarmi dei parametri e parametri mini può mandare come questi ad esempio la server che voglio chiamare 
    papà per questa è la querystring e da questo nome valore che sei forte non ne vanno selezionate la prima cosa che 
    dovevo fare come sempre in lunghezza sarà quello di andare a leggere andare a leggere i parametri ricordiamoci 
    come ho fatto una marea questo ho fatto con 1 l è una serie di eder e poi tra cui ci possono essere 
    autenticazioni concessione che vedremo in seguito questo è impossibile abbiamo già visto un altro volto questo è 
    impossibile messaggio gap di richiesta dove c'è il metodo cielo c'è la guerra il patto dell'aser lett e i 
    parametri abbiamo lost sempre che deve rispondere è anche il mezzo la versione http ci sono cose che abbiamo 
    visto la re questa in realtà una serie detto non lava assettare però mi ha salvato non 7 una legge ma lei questo 
    mi viene dal browser fare questo viene scritta dal client e viene inviata al al flyer il client come tipica 
    azione legge le informazioni che gli ha mandato fuori abbiamo visto che le modelle mvc il server può scrivere 
    degli attributi da mandare poi alla view quali sono i metodi ecco se li il goethe eder che avevamo prima cioè 
    prima avevamo un 7 eder sulla risposta qui abbiamo un gap ed esperti come dicevo nel lato server io voglio 
    leggere non è che se volete sapere qual è lo user agent ricordatevi che attendere strisce ma stringa posso fare 
    questo comando se voglio sapere quali cui chi mi ha mandato il browser faccione questo gap cupis e questo mi 
    viene dato il murray gli oggetti di tipo se voglio sapere se c'è una sessione attiva io faccio né queste location 
    non sotto vedremo che cosa significa non è semplice e immediato da spiegare che troverebbero e voglio sapere se 
    quale metodo mi è stato inviato c'è un ghetto un posto o da fare death metal per voglio leggere un input stream 
    che viene mandato nella referto faccio questo quindi come vedete ci sono molti molti sono i metodi che si possono 
    utilizzare per questo che ha inizio la lezione vi ho dato e riferimenti dove approfondire ora per ognuna di 
    questa ricercare su questo ci vengono dati i programmi ed esempi di riccio quindi vi esorto a utilizzarli trovar 
    di voi stessi e vedere come si comporta una cosa volevo dirvi i parametri non li possiamo non c'è bisogno di un 
    forno per mandare i parametri con un fondo sicuramente siamo in grado di mandare i forti i parametri ma io li 
    potrei anche mandare fa una carezza con ancora dove metto come href tutta la rl che il mio browser creerebbe da 
    un forum addirittura io questo lo posso mettere così come nel browser direttamente nell'ufficio che para meter è 
    quello che noi utilizziamo e già lo sapete per andare a leggere i valori quindi se e questa è la richiesta da 
    questo punto che par hunter altri da massimo di 120 i metodi della richiesta per accedere para ne sono che para 
    meter che è pronta expect that were here string che è in poiché fatto io vi esorto che abbassando perioso di 
    leggere queste cose risorto approvarli cioè noi abbiamo già fatto che già fatto vedere che tenete in quella 
    applicazione andate ad aggiungere queste chiamate e pubblicate nella jazz fin qui potete fare queste cose così 
    che vedete data una richiesta che cosa hai riesce in altri sono esercizi utili per prendere come facciamo per 
    prendere dimestichezza complessi con questi mesi ragazzi come facciamo a conoscere tutti gli haters in realtà c'è 
    un qui non c'è non è mutato c'è la caccia headers all as laga headers diavolo trovo qui sulla richiesta o per chi 
    magari ha ragione sono tipo nelle slide non posso di fare più danno sarebbe questo gap eder ponendo che bacio 
    comunque c'è un metodo che si chiama che verso culturale che vi permette di anche se non sapete i nomi di 
    estrarre tutte le coppie nome campo valore campo e voi poi ad un alunno di scontato in realtà noi questo lo 
    abbiamo già fatto nell'applicazione degli scoppiati provato sulla carta per che abbiamo altra cosa è il metodo 
    che abbiamo utilizzato adesso come faccio a rivedere nel controller fare quest'età opposto per cronica e questo 
    lo abbiamo fatto l'interesse più di lizzanello nel nome l'anti spa qui abbiamo usato la variabile eder che 
    contiene tutti gli hater della richiesta però e questo lo abbiamo fatto con js pl quindi la domanda che mi è 
    stata fatta come faccio a sapere visto che ogni browser mi manda tanti in headers ogni frase manda i suoi haters 
    come faccio io lato server a leggere in particolare nome di sel se non non so quali vi sta mandando allora se 
    volete usare la sp per visualizzarli potete utilizzare per questa variabile che mi ha messo a disposizione ma tra 
    poco vedremo che esiste il messaggio in un modo anche tramite server di avere di avere ecco allora molti chiamati 
    ad est ma si chiama i termini questo codice indipendentemente da quali chiedermi manda un browser o qualsiasi 
    browser io riesco a stamparsi sempre tutti così com'era prima nella jazz più allora vedete names uguale re questo 
    quindi questa è la numerazione di stringhe chiede ed ernest va a prendere tutti gli header della richiesta che mi 
    sa che mi è arrivata può essere una richiesta che mi ha tenuto da safari una richiesta con 10 headers è vero 10 è 
    così poi faccio un interazione per questo names e vedete che per ogni mail lancio del nederlands e prende più 
    valori perché perché uno stesso uno stesso eder potrebbe avere più di un valore quindi io devo devo prevedere il 
    fatto che ce ne possono essere fighi e quindi metto la nomination sebbene e poi stampo ciascun valore che 
    corrispondendo a quel nome del never think and various a più elementi allora vado a stampare il valore e qui c'è 
    un sistema out ma non è corretto questo scrive sulla console poi non l'ha usate così usate la utilizzando auto se 
    volete come questo che viene dopo è tutta roba che abbiamo già conosciamo molto bene quindi se io mando questa rl 
    dell'amia per poter leggere mario col nella durezza mio parere questo è uno che spara meter tu dove tu e questo 
    nome e tu ne diventa mario poi posso scrivere questo tool m la risposta è il messaggio di risposta per viene 
    inviato al client sarà questo con la parola mario quindi notate che quello che viene mandato al client è sempre 
    codice pulito html senza senza frontiere almeno che ci sia già fa scritto cioè senza poter però che questo mario 
    si è uscito fuori dall esecuzione di una serra e kio client non lo posso sapere io vedo direttamente il nome 
    mario potrebbe essere uscito fuori da un campo che non serve molto complesso ma io client e non me ne accorge di 
    ottengo direttamente il collettivo il tuo posto funziona tale attuale e pretendere il valore di floris nella 
    classe nei nel posso fare la stessa cosa qui sto leggendo un stream che mi viene male quindi significa che nella 
    richiesta mi è stato mandato dal sull'hard disk adesso sull'hard disk non viene eliminata perchè dovrebbe qual è 
    il vantaggio di questa cosa e quando che accendo il server non c'è da fare la traduzione da java ha classe quante 
    poi lo si può fare anche dalle file ma nadal dal tomcat manager tante poi si può anche fare direttamente dal 
    tomcat manager oppure parlato ieri a mano non c'è il tempo di fare nulla sì sì in effetti si guardava cancello la 
    cartella faccio l'under gloria luciano ancora in memoria quando si accorge che non c'è più sul file system lancia 
    il destro e elimina la cartella elimina anche la rappresentazione in memoria fa la garbacz collection 
    dell'oggetto il test roy non viene chiamato solo quando si fa lo shutdown viene chiamato ogni volta che io faccio 
    lande prodi un'applicazione mangiati essere google ci avete un server acceso tutti i giorni mi viene data 
    un'applicazione per la mettete poi mi avete fatto una nuova la vecchia la togliete state facendo tante prove e 
    mettete la nuova ma serve al nono spegnete questo è il discorso visto viene chiamato quando si fa lante floyd e 
    l'applicazione quando lo levo dalla crisi quando l'avevo dalla cartella lo shutdown può prevedere lande prova di 
    tutte le applicazioni e poi la chiusura non vi confondete tra che non sono la stessa cosa lande placa lo scià da 
    quando faccio lo shutdown posso anche fare l'anteriore posso fare l'under blog dante blog di un'applicazione ma 
    senza fare lo showdown del 7 valorizzate minima dove vi è chiaro adesso facilmente rifatemi una domanda e io la 
    leggerò ok torniamo alla spalla destra adesso comunque continuiamo a parlarne perché la presentazione continua su 
    questo allora vedete che questa schermata ci sono i momenti più importanti della del ciclo di vita di una serie 
    comunque sono training services e destro sembra memoria sotto le cose da ricordare in it quando nasce service 
    quando viene usata testo ai quanto muro quindi il container chiamava i meet quando è chiamato dai dopo che le 
    stanza è stata creata istantanee di una server integrato una sola volta nella sua vita ma prima che la server 
    possa servire una qualsiasi richiesta ma che serve perché dovrei lancia thema in it solo una volta all'insù ma 
    sempre perché mi dà la possibilità di inizializzare la serie poi suoi dati prima che chiunque altro inizia a fare 
    richieste un esempio classico è quello che io potrei potrei aver bisogno di con una serve perché si connette ad 
    et autres quindi cosa potrei fare la connessione la faccio una sola volta quando viene inizializzata la pelle poi 
    tutte le chiamate che io faccio a quella se flat che i clienti fanno a quella terra trovano la connessione a 
    conte tabet è già già fatta e non è che ogni richiesta mi deve aprire il depresso perché io l'ho fatta all'inizio 
    quando ho messo a disposizione alla terra quando poi questo serve che magari è diventata vecchia obsoleta 
    l'abbiamo sostituibile io faccio lande pro cioè la leggo che cosa succede che quando chiedono tomcat di fare 
    lande flory lui mi manda il destro e io programmatore cosa ho programmato nella versione ho programmato la 
    chiusura della pensionate rapezzi quindi questa serie black quando nasce a tre dell'ardens la connessione quando 
    muore chiude la connessione le fattezze e invita fa query interagisce cose d'altri allora la posso sovrascrivere 
    line it allora finora non abbiamo mai soprascritta però se ho questa esigenza che vi ho appena detto io posso 
    posso sovrascriverà non è un problema ok quindi ogni volta che io devo o magari vedete connessione al database o 
    mi devo registrare con dei con altri oggetti o devo inizializzare delle variabili che dovranno essere utilizzate 
    per tutta la vita della serve allora io posso solo a scrivere il metodo enit nella mia classe quindi lì dove 
    abbiamo scritto to get metto anche in poi c'è il metodo service il metodo service può essere lanciato da una 
    serve solo se limits ha finito ora se io non lo so trascrivo un'inter già esiste in contatto lo chiama comunque 
    magari non fanno una fase cose ma c'è non fa nulla è però lo chiama lo stesso quando lo hai mit ha finito la sua 
    esecuzione è possibile per la per la serietà accettare service duchetto tifoso quindi quasi inizia apparire la 
    parola fretta perché quando viene chiamato unasur lett in realtà viene creato una serve per ogni utente che fa la 
    richiesta quindi al container vedremo fra poco così come succede inizia un thread collega un 3 dell'album e causa 
    la chiamata della della serie serve pratola server service appare a sua volta l'ha chiamata ad un ghetto ad posto 
    secondo che il metodo http era ghetto poi lo posso sovrascrivere no ma service non la dobbiamo assolutamente 
    toccare noi lavoriamo su ducati e 2 posto ma il service non lo dobbiamo ok quindi la emi psi la service no l'aduc 
    ha potuto post assolutamente se vogliamo che faccia qualcosa che possiamo fare quindi qui è dove c'è il mio 
    codice quindi in quanto è chiamata è chiamato a service a che serve serve per dire alla serie b cosa fate fatica 
    e la dobbiamo sempre si fa schifo ok ora arriviamo al multi threading naturalmente ora la mia domanda è quanti di 
    voi sanno cos'è una serve un thread ragazzi c'è qualcuno che sa cos'è che non sa cos'è un thread fino adesso ai 
    vari ma mi si chiede se devo sovrascrivere il metodo le storiche per chiudere una connessione dei tab e se sì ad 
    esempio si anche destroy non c'era scudetto post fino alle storie come la indicava va riscritta ok posso dare per 
    scontato che sapete cos'è un prezzo quindi il trend è un processo leggero nel senso che nel senso che io ogni 
    volta che se solo in un ambiente multi utente più utenti vogliono di rifare lo stesso eseguire lo stesso codice 
    io posso staccare un thread creare un flat per ogni utente perché il leggero perché questo thread e seguirà lo 
    stesso codice per ogni utente ma potrà avere non replica ma memoria genere tutta la memoria del della foto chica 
    voi avete scritto quindi il centro blè ma che se più utenti e seguono lo stesso codice sugli stessi dati c'è un 
    problema di voi conoscete questo di sezione critica cioè non concorrenza di accessi concorrenti sbagliati quindi 
    il thread se non programmate bene il 3 il vostro vostro tablet alcuni dati potrebbero essere [Musica] andare in 
    uno stato incompatibile perché vengono modificate da più persone simultaneamente questo quello che lo vedremo più 
    in dettaglio il modello normale per una server che io poi posso anche decidere se vogliono maschi trading così 
    non lo voglio di default il server container fa mastiff rating per cui ad ogni richiesta viene attaccata una un 
    freddo viene creato il trade e quella è quella richiesta viene soddisfatta perché per quello specifico utente è 
    una modalità normale più stretto condividono la stessa istanza di una server e quindi si crea una situazione di 
    concorrenza sul limite non c'è concorrenza perché ne ho già detto che prima che io possa accedere ma i neet è già 
    stata eseguita quindi viene seguito una sola volta quando la serba è caricata web containers quindi sul l'init 
    non c'è malti serio e sui service che possono essere sensi dice possono essere chiamati soltanto dopo beh ma 
    anche la destra viene chiamata una sola volta e non c'è concorrenza cioè nel momento in cui celante flory il 
    destro e viene viene annunciato non c'è non è un utente che decide che può far fare l'andatura di una 
    applicazione è l'amministratore di tom kat che decide che con l'applicazione deve essere tolta e allora tocca 
    prima di liberare la memoria per quell'applicazione bilancio ed estrosi la concorrenza invece geloso di studente 
    sul posto quindi durante proposto può essere invocata da numerosi clienti il service in moto componente quindi 
    sul service che abbiamo molti preti e quindi è necessario gestire le sezioni critiche quindi se io in quella 
    classe o una variabile un dato membro della classe non dato locale a un metodo ma un dato che è della classe e 
    allora su quello si può creare si può creare un problema se più e più utenti vogliono accedere a quella variabile 
    e quindi si usa in java synchronize avete mai usato synchronized in java ok lo vedremo raramente però ok adesso 
    vediamo quindi il modello il container e segue malti mostra per processare richieste non c'è il cliente a il 
    cliente b entrambi si collegano vogliono accedere alla stessa sampler perché li mangiano la cattura rekuest al 
    web server che la passa al container e il container individuo la server e prima di fare il controllo di far 
    eseguire la serve naturalmente la serve salerno stato già inizializzato ok quindi il container la cerca tra 
    quelle già inizializzate se la trova crea un thread per cliente acre altre da perché inter di tre il trend il 
    test che se non ci sono problemi di concorrenza queste due non c'è bisogno di scrivere il numero di synchronized 
    nella sezione critica non ho bisogno di creare tensioni critiche se non ci sono si accede concorrenza i due 
    thread e servono ciascuno la sempre per il proprio ambiente creano una risposta è la mandano al browser quindi 
    ciascun cliente ottiene un thread separato e ciascuno quindi avrà una risposta è diversa non vi preoccupate per 
    il synchronizer quando lo vedremo sarà semplice per cento per lo scrivere su java abbastanza semplice però non è 
    una cosa focale di questo punto possiamo anche dire a se non vogliamo gestire la concorrenza quindi con il 
    synchronize io potrei dire a tom bucato di non essere molti preti ok però questo è un problema perché è un 
    problema di efficienza in realtà solo di efficienza perché in questo modello che vedete il server non potrà 
    essere e non potrà servire sia a e b in modo concorrente ma serve lettura prima eseguire la eseguire per il 
    cliente ha magari è solo dopo che ha finito passare il controllo andare a soddisfare per la richiesta del cliente 
    più la concorrenza sia anche quando non si siano dati polmoni perché si chiama lo stesso codice però il problema 
    della concorrenza sia quando si hanno dati come se non ai dati comuni possono lavorare tranquillamente con 
    correntemente ma non c'è non\n
    Domanda: Il 500 che tipo di errore riporta?\n
    Opzione1: 500 significa che la risorsa è presente in un altro URL\n
    Opzione2: 500 significa che la richiesta HTTP ha avuto buon esito\n
    Opzione3: 500 significa che c'è un problema nella vostra servlet\n
    Opzione4: 500 significa che la risorsa non è stata trovata\n
    Risposta: 500 significa che c'è un problema nella vostra servlet\n
    Valutazione: CORRETTA\n\n'''

    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(esempio=esempio, transcript=docs_page_content, opzione1=opzione1,
                         opzione2=opzione2, opzione3=opzione3, opzione4=opzione4, domanda=domanda, risposta=risposta)

    print("Processo di valutazione completato!")

    response = response.replace("\n", "")

    return response


# CREA QUIZ IN FORMATO MOODLE
def moodle_quiz_creator(db, request, argomenti, k=4):
    docs_page_content = db.similarity_search(argomenti, k=k)
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.5)
    template = """Ciao ChatGPT, tu sei un creatore di quiz nel formato MOODLE XML.

        Le risposte corrette hanno un valore per 'fraction' pari a 100. Le risposte errate hanno un valore pari a 0.

        Le domande del quiz devono essere formulate sul seguente testo: {transcript}."""
    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "Crea un quiz sul testo fornito in formato MOODLE XML composto da:\n{user_request}."
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )
    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(transcript=docs_page_content, user_request=request)

    print("Creazione quiz in formato Moodle completata.")

    # Utilizza espressione regolare per estrarre l'XML
    match = re.search(r'\s*<quiz>.*?</quiz>', response, re.DOTALL)

    if match:
        xml_codice = match.group(0)
        intestazione = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        final_response = intestazione + xml_codice
        return final_response
    return "errore"


# CREA QUIZ IN FORMATO AIKEN
def aiken_quiz_creator(db, request, argomenti, k=4):
    docs_page_content = db.similarity_search(argomenti, k=k)
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.5)
    template = """Ciao ChatGPT, tu sei un creatore di quiz nel formato AIKEN.

    Le domande del quiz devono basarsi sul seguente testo: {transcript}"""
    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    human_template = "Rispondi solo ed esclusivamente col quiz in formato Aiken strutturato come in questo esempio: {esempio}\n" \
                     "Il quiz deve essere composto da {user_request}."
    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)
    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )

    esempio = """Qual è l'acronimo di HTML?
    A. HyperText Markup Language
    B. HyperText Transfer Protocol
    C. Highly Typed Multi Language
    D. Hyper Transfer Markup Language
    ANSWER: A"""

    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(transcript=docs_page_content, user_request=request, esempio=esempio)

    print("Creazione quiz in formato Aiken completata.")

    return response


# Decorator in Flask che associa la funzione scegli_lezione() all'endpoint /scegli_lezione e specifica che la richiesta
# deve essere di tipo POST.
@app.route('/scegli_lezione', methods=['POST'])
def scegli_lezione():
    url_video = request.form['lesson']
    print("\n\nUrl lezione scelto: ", url_video)

    print("\nCreazione database vettoriale sulla base della lezione scelta...")
    # variabile application scope (NB: ridondante il salvataggio=ì)
    app.config['page_content'] = create_db_from_youtube_video_url(url_video)

    print("\nDatabase creato correttamente! In attesa dell'utilizzo dell'applicazione...")

    return "Trascrizione effettuata correttamente!"


@app.route('/chatbot', methods=['POST'])
def chatbot():
    query = request.form['query']
    print("Domanda dell'utente: ", query)

    db = app.config['database']

    return get_response_from_query(db, query)


@app.route('/quizCreator', methods=['POST'])
def quizCreator():
    print("Richiesta pagina web per quiz accolta!")
    return quiz_creator()


@app.route('/quizMoodle', methods=['POST'])
def quizMoodleCreator():
    rispostaMultipla = request.form['multiple']
    trueFalse = request.form['truefalse']
    matching = request.form['matching']
    shortAnswer = request.form['shortanswer']
    numerical = request.form['numerical']
    essay = request.form['essay']
    argomenti = request.form['argomenti']

    print("Argomenti: ", argomenti)

    print("Creazione quiz in formato Moodle...")
    compQuiz = ""

    if int(rispostaMultipla) > 0:
        compQuiz += "- " + rispostaMultipla + " domanda/e di tipo 'multichoice'\n"
    if int(trueFalse) > 0:
        compQuiz += "- " + trueFalse + " domanda/e di tipo 'true/false'\n"
    if int(matching) > 0:
        compQuiz += "- " + matching + " domanda/e di tipo 'matching'\n"
    if int(shortAnswer) > 0:
        compQuiz += "- " + shortAnswer + " domanda/e di tipo 'short answer'\n"
    if int(numerical) > 0:
        compQuiz += "- " + numerical + " domanda/e di tipo 'numerical'\n"
    if int(essay) > 0:
        compQuiz += "- " + essay + " domanda/e di tipo 'essay'\n"

    db = app.config['database']

    return moodle_quiz_creator(db, compQuiz, argomenti)


@app.route('/quizAiken', methods=['POST'])
def quizAikenCreator():
    rispostaMultipla = request.form['multiple']
    argomenti = request.form['argomenti']

    print("Argomenti: ", argomenti)

    if rispostaMultipla == "1":
        compQuiz = "una sola domanda"
        print("Richiesto file in formato Aiken con una sola domanda")
    else:
        compQuiz = rispostaMultipla + " domande a risposta multipla"

    db = app.config['database']

    print("Creazione quiz in formato Aiken...")

    return aiken_quiz_creator(db, compQuiz, argomenti)


@app.route('/returnUrlPhoto', methods=['POST'])
def returnUrlPhoto():
    url_video = request.form['lesson']

    id_video = get_video_id_from_url(url_video)

    return get_video_info(id_video)


@app.route('/returnIdVideo', methods=['POST'])
def returnIdVideo():
    url_video = request.form['lesson']

    return str(get_video_id_from_url(url_video))


@app.route('/valutaRisposta', methods=['POST'])
def valutaRisposta():
    opzione1 = request.form['opzione1']
    opzione2 = request.form['opzione2']
    opzione3 = request.form['opzione3']
    opzione4 = request.form['opzione4']
    domanda = request.form['domanda']
    risposta = request.form['risposta']
    print("Domanda dell'utente: ", domanda)
    print("Risposta dell'utente: ", risposta)

    db = app.config['database']

    return valutaRispostaQuiz(db, opzione1, opzione2, opzione3, opzione4, domanda, risposta)


if __name__ == '__main__':
    app.run(port=5001)  # Flask in ascolto sulla porta 5001