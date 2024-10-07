# Note Manager

## Описание / Overview

Этот проект — решение лабораторного задания по программированию на Java - простое CLI клиент-серверное приложение для управления заметками через TCP-соединение. Клиенты могут подключаться к серверу, авторизовываться, создавать заметки, редактировать их, просматривать список заметок, а также делиться заметками с другими пользователями.

This project is a a solution for a laboratory assignment in Java programming - simple CLI client-server application for managing notes using TCP connections. Users can connect to the server, authenticate, create notes, view their list of notes, edit, delete, and share them with other users. 

## Основные возможности / Key Features

- Регистрация и вход пользователей / User registration and login.
- Создание и редактирование заметок / Creating and editing notes.
- Удаление и просмотр заметок / Viewing and deleting notes.
- Возможность делиться заметками с другими пользователями / Sharing notes with other users.
- Поддержка многопользовательского режима с сохранением данных на сервере / Multi-client support with data persistence on the server.

## Структура проекта / Project Structure

### Сервер / Server

Серверная часть отвечает за управление подключениями, обработку команд от пользователей и хранение информации о заметках и пользователях. Основной класс сервера:

The server handles client connections, processes user commands, and stores information about users and their notes. The main server class:

- **NoteServer**: Управляет всеми клиентами и сохранением данных пользователей. Подключение происходит через сокеты, а информация о пользователях хранится в сериализованном виде для последующей загрузки.

  Manages all clients and stores user data. Connections are made through sockets, and user information is saved using serialization for later use.

### Клиент / Client

Клиентская часть реализует консольный интерфейс. Пользователь может подключиться к серверу и выполнить различные операции с заметками через консоль.

The client provides a console-based interface where users can connect to the server and perform various operations on notes.

- **NoteClient**: Устанавливает соединение с сервером и отправляет команды от пользователя для выполнения на сервере.

  Establishes a connection to the server and sends user commands to the server for processing.

## Как запустить / How to Run

### Сервер / Server

1. Перейдите в папку `lab4_server` и выполните команду:
   ```bash
   java -cp ./src NoteServer
   ```
   Сервер будет прослушивать подключения на порту 12345.

1. Go to the lab4_server folder and run:
    ```bash
    java -cp ./src NoteServer
    ```
  The server will listen for connections on port 12345.

### Клиент / Client

1. Перейдите в папку lab4_client и выполните команду:

  ```bash
  java -cp ./src NoteClient
  ```
1. Go to the lab4_client folder and run:

  ```bash
  java -cp ./src NoteClient
  ```



# TO-DO
  ### Add TLS and encryption of user files (In Progress)
  ### Add GUI 
