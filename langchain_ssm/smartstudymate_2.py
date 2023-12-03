from datetime import timedelta
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
import re
from youtube_transcript_api import YouTubeTranscriptApi

# Chiave API di OpenAI
os.environ["OPENAI_API_KEY"] = "personal_openai_api_key"

embeddings = OpenAIEmbeddings()
app = Flask(__name__)

# Chiave API di YouTube
api_key = "personal_youtube_api_key"

# Connessione all'API di YouTube
youtube = build('youtube', 'v3', developerKey=api_key)


class Document:
    def __init__(self, page_content, metadata):
        self.page_content = page_content
        self.metadata = metadata


def return_temporized_trascript(video_id, i):
    temporized_trascript = ""
    only_text = ""

    # Ottieni la trascrizione del video (che sia essa in italiano o in inglese)
    transcript = YouTubeTranscriptApi.get_transcript(video_id, languages=["it", "en"])

    for entry in transcript:
        start_time_seconds = entry['start']
        text = entry['text']
        only_text += text
        tempo_formattato = str(timedelta(seconds=start_time_seconds))
        parti_tempo = tempo_formattato.split(":")
        minute_of_video = f"[{i} - {parti_tempo[0]}:{parti_tempo[1]}:{parti_tempo[2][:2]}]"
        temporized_trascript += f"{minute_of_video}" + "" + text + " "

    return temporized_trascript


def create_dbs_from_youtube_video_url():
    transcript = ""
    i = 0
    list_dbs = []
    list_url = app.config['videos']
    for video_url in list_url:
        print(f"Video n.", i)
        id_video = YoutubeLoader.extract_video_id(video_url)
        transcript += return_temporized_trascript(id_video, i)
        print("Ho la trascrizione del video...")
        transcript_docs = {Document(page_content=transcript, metadata={"id_video": id_video})}
        text_splitter = RecursiveCharacterTextSplitter(chunk_size=2000, chunk_overlap=400)
        docs = text_splitter.split_documents(transcript_docs)
        print("Ho creato il docs per il video...")
        db = FAISS.from_documents(docs, embeddings)
        print("Ho creato il db per il video...\n")
        list_dbs.append(db)
        i += 1
        # Ci fermiamo alla creazione di soli due db per questioni legate all'utilizzo delle API di OpenAI e alla
        # quantità di richieste effettuabili con in piano gratuito
        # if i > len(list_url)-1:
        if i > 2:
            app.config['dbs'] = list_dbs
            break


# CREA QUIZ IN FORMATO MOODLE
def moodle_quiz_creator(request, argomenti):
    docs_page_content = ""
    docs_page_content += get_similarity_from_query(argomenti)
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0)
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
def aiken_quiz_creator(request, argomenti):
    docs_page_content = ""
    docs_page_content += get_similarity_from_query(argomenti)
    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0)
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


def get_similarity_from_query(query, k=4):
    docs_page_content = ""
    list_dbs = app.config['dbs']
    for db in list_dbs:
        docs = db.similarity_search(query, k=k)
        print(f"Db similarity effettuata...\n")
        docs_page_content += " ".join([d.page_content for d in docs])

    return docs_page_content


# RICAVA IL LA VIDEOLEZIONE ED IL MINUTO IN CUI SI ESPONE PASSATO COME PARAMETRO
@app.route('/search-engine', methods=['POST'])
def get_response_from_topic():
    topic = request.form['topic']
    print("topic cercato: ", topic)
    docs_page_content = ""

    docs_page_content += get_similarity_from_query(topic)

    chat = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.2)

    template = """Ciao ChatGPT, tu in questo momento sei un motore di ricerca per contenuti che, sulla base di un 
        topic fornito dall'utente, fornisce come risultato la lezione e l'intervallo di tempo nel quale si inizia ad esporre quel topic.

                    La trascrizione che ti sarà fornita conterrà delle etichette seguite da testo. Le etichette fanno riferimento alla lezione
                    ed al minutaggio della lezione. La struttura dell'etichetta è la seguente: [numero_lezione - hh:mm:ss].

                    Ecco la trascrizione sulla quale dovrai lavorare sulla base del topic fornito dall'utente: {docs} """

    system_message_prompt = SystemMessagePromptTemplate.from_template(template)

    human_template = """Topic: {question}. La tua risposta dovrà riportare il numero della lezione in cui si espone 
        il topic fornito dall'utente. Devi fornire il tempo nel formato [hh:mm:ss] da dove inizia 
        l'esposzione di quel topic. Alla fine fai un summary sul topic."""

    human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)

    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )

    chain = LLMChain(llm=chat, prompt=chat_prompt)
    response = chain.run(question=topic, docs=docs_page_content)
    response = response.replace("\n", "")
    return response


# SULLA BASE DELLA RISPOSTA RICEVUTA DAL MEDOTO "get_response_from_topic" ESTRAE I MINUTI E LI CONVERTE IN SECONDI
@app.route('/second-time', methods=['POST'])
def return_second_topic():
    text = request.form['text']
    chat2 = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.2)
    template2 = """Fornisci l'output dell'ultimo input stampando solo ed esclusivamente i secondi, senza aggiungere 
    altro."""

    system_message_prompt2 = SystemMessagePromptTemplate.from_template(template2)
    human_template2 = """Input: Il topic Linear Regression viene trattato a partire dal minuto 00:04:30.
                        Output: 270s
                        Input: Il topic Machine Learning viene trattato nella lezione 0 a partire dal minuto 00:09:45
                        Output: 985s
                        Input: {message}
                        Output:"""
    human_message_prompt2 = HumanMessagePromptTemplate.from_template(human_template2)
    chat_prompt2 = ChatPromptTemplate.from_messages(
        [system_message_prompt2, human_message_prompt2]
    )
    chain2 = LLMChain(llm=chat2, prompt=chat_prompt2)
    second = chain2.run(message=text)
    return second


# SULLA BASE DELLA RISPOSTA RICEVUTA DAL MEDOTO "get_response_from_topic" ESTRAE IL NUMERO DELLA LEZIONE
@app.route('/number_lesson', methods=['POST'])
def return_number_lesson():
    text = request.form['text']
    chat3 = ChatOpenAI(model_name="gpt-3.5-turbo-16k", temperature=0.2)
    template3 = """Fornisci l'output dell'ultimo input stampando solo ed esclusivamente il numero della lezione, 
    senza aggiungere altro."""
    system_message_prompt3 = SystemMessagePromptTemplate.from_template(template3)
    human_template3 = """Input: Il topic Linear Regression viene trattato nella lezione 0 a partire dal minuto 00:04:30.
                         Output: 0
                         Input: Il topic Machine Learning viene trattato nella lezione 1 a partire dal minuto 00:09:45
                         Output: 1
                         Input: {message}
                         Output:"""
    human_message_prompt3 = HumanMessagePromptTemplate.from_template(human_template3)
    chat_prompt3 = ChatPromptTemplate.from_messages(
        [system_message_prompt3, human_message_prompt3]
    )
    chain3 = LLMChain(llm=chat3, prompt=chat_prompt3)
    lesson = chain3.run(message=text)
    return lesson


# PERMETTE DI INSERIRE UN NUOVO URL DI UNA VIDEOLEZIONE NELLA VARIABILE GLOBALE "list_url"
@app.route('/insert_lesson', methods=['POST'])
def insert_lesson():
    url_video = request.form['lesson']
    list = app.config['videos']
    list.append(url_video)
    print("Ricevuto!")
    print("Lista video: ", list)

    create_dbs_from_youtube_video_url()
    print("database creato correttamente")

    return "all good!"


# CONSENTE DI INSERIRE GLI URL NELLA VARIABILE GLOBALE "list_url" AL LOGIN DI UN NUOVO DOCENTE
@app.route('/initialize_list_urls', methods=['POST'])
def initialize_list_urls():
    url_video = request.form['lesson']
    last_url = request.form['last']

    list = app.config['videos']
    list.append(url_video)
    print("Ricevuto!")
    print("Lista video: ", list)

    if last_url == "si":
        create_dbs_from_youtube_video_url()
        print("database creato correttamente")

    return "all good!"


# FORNISCE COME RISPOSTA IL QUIZ RICHIESTO IN FORMATO MOODLE XML
@app.route('/quizMoodle', methods=['POST'])
def quizMoodleCreator():
    rispostaMultipla = request.form['multiple']
    trueFalse = request.form['truefalse']
    matching = request.form['shortanswer']
    shortAnswer = request.form['matching']
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

    return moodle_quiz_creator(compQuiz, argomenti)


# FORNISCE COME RISPOSTA IL QUIZ RICHIESTO IN FORMATO AIKEN
@app.route('/quizAiken', methods=['POST'])
def quizAikenCreator():
    rispostaMultipla = request.form['multiple']
    argomenti = request.form['argomenti']

    print("Argomenti: ", argomenti)

    compQuiz = rispostaMultipla + " domanda/e a risposta multipla."

    print("Creazione quiz in formato Aiken...")

    return aiken_quiz_creator(compQuiz, argomenti)


# Resetta le variabili globali
@app.route('/logout', methods=['POST'])
def logout():
    print(request.form['request'])
    app.config['videos'] = list_url
    list_url.clear()
    return "all good!"


if __name__ == '__main__':
    list_url = []
    app.config['videos'] = list_url
    app.run(port=5002)  # Flask in ascolto sulla porta 5002
