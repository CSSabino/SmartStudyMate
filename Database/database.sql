create database smartstudymate;
use smartstudymate;

create table docente(
	primo_accesso boolean not null,
	email varchar(80) primary key,
    nome varchar(64) not null,
    cognome varchar(64) not null,
    passwordHash varchar(50) not null
);

create table videolezione(
	url varchar(200) primary key,
    titolo varchar(64) not null,
    descrizione varchar(200) not null,
    url_photo varchar(200),
    access_code varchar(50),
    video_embeded varchar(200),
    proprietario varchar(80) references docente(email)
);


-- Credenziali per l'accesso come insegnante a SmartStudyMate (al primo accesso bisogna cambiare la password)
insert into docente values (true, "prova@gmail.com", "Prova", "Prova", "cambiami");
insert into docente values (true, "prova1@gmail.com", "Prova1", "Prova1", "cambiami");
insert into docente values (true, "prova2@gmail.com", "Prova2", "Prova2", "cambiami");