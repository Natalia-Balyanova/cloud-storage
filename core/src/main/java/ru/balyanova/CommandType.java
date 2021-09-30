package ru.balyanova;

public enum CommandType {
    FILE_MESSAGE, //посылка файла возьми файл.. upload/ с клиента на сервер. и на сервере и на клиенте
    FILE_REQUEST, //запрос дай мне файл с именем.. download. только с клиента на сервер
    LIST_REQUEST, //клиент отправляет, дай мне список, который у тебя есть без параметров. только с клиента на сервер
    LIST_RESPONSE, //ответ держи сам список, клиент принимает только лимт респонс. только с севрера на клиент
    PATH_REQUEST, // клиент на сервер шлет запрос пути без параметров. только с клиента на сервер
    PATH_RESPONSE, //сервер отвечает в какой директории находится с параметром. только с севрера на клиннт
    GO_TO_PATH_REQUEST, //
    MOVE_TO_DIR,// double click on server, htpekmnfnjv ,eltn path_response
    CLOUD_FILESLIST,
    DOWNLOAD,
    UPLOAD,
    READY_TO_UPLOAD;
}
