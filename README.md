# 💬 Chat XMPP for alumchat.lol server

For this project I did a Java application to chat using XMPP protocol. Here you will find some technologies used in the project that waas built in Java Language, and the instructions tu use it in your computer.

### 📚 Libraries
- Smack
- JXMPP
- Spring Framework

### 📱 Project Sections
- `src/main/java/com/proyecto1redes/demoproyecto1`: In this folder are the `.java` files, where are the principal functions using Spring Boot framework. There are funcions like receiving and sending messages, login and logout, sign in with a new account, receiving and sending ypu presence, web sockets connfig, etc.
  -   `Demoproyecto1Application.java`: The main function to run the project.
  -   `XMPPConnection.java`: File with the login, sign in and logout connection to the server.
  -   `LoginController.java`: File with all the functionalities that you can do while you are logged in.
  -   `SecurityConfig.java`: File that contains the securiti config to connect to the XMPP server.
  -   `WebSocketConfig.java`: File with the web sockets configuration.
  -   `WebSocketController.java`: File that recieves or sends the information to the backend or the front end like, messages or presences.
  -   `ChatMessage.java`, `OwnPresenceMessage.java`, `PresenceMessage.java`: Files with the structure, getters and setters of every message structure.
- `src/main/resources/templates`: In this folder are the HTML files to have the web application structure.
- `src/main/resources/static/css`: There are the CSS styles for the HTML.
- `src/main/resources/static/js`: There are the JS files to connect the backend app with frontend functions using web sockets.
- `pom.xml`: File where you put the dependencies that the application uses importing the libraries.

### 📲 Functionalities
Some of the functionalities that you can use in this chat application.
- Create a new account
- Log in with an account
- Log out whith your account
- Delete your own account from the server
- Add new contacts
- See contact's mode and status (presence)
- Change your own status and mode
- Chat with your contacts

### 💻 Installing this project in your device
There are some steps that you can follow if you want to use this chat application
1. Clone this repository
   ```bash
   git clone https://github.com/LINDAINES213/Chat_XMPP.git
   ```
2. If you don't have Java in your device, you only need to install the `jdk` of your choice, using a `.zip` or an `.exe` file
3. If you have already this project folder in your device, open it with your favorite IDE and search for the `Demoproyecto1Application.java` in the following path: `src/main/java/com/proyecto1redes/demoproyecto1`. When you open it, you run it an you'll have running the application in the `http://localhost:2422/` URL. You can change the port in the following file: `src/main/resources/application.properties`.

If you did everything okay you'll have running the application, and if you want you can improve and add more functionalities in the files that I mentioned before. An aditional note is that the when you login, the contact list will show only if one of you contacts change its presence, if don't the contact lit will appear empty ultil this happens. Enjoy! <br><br>
![image](https://github.com/user-attachments/assets/45087d1b-0448-4a9d-b64c-9ee288af6514)
