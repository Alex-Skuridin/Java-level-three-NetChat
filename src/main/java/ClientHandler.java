import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Этот
public class ClientHandler extends Thread implements Closeable {


    // Поля класса
    private MyServer server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name = "unknown";
    private boolean isAuth = false;
    private LocalDateTime connectTime = LocalDateTime.now();

    // Конструктор класса. В качестве аргументов принимает серверный сокет и клиентский сокет
    public ClientHandler(MyServer server, Socket socket) {

        //Присваиваем полю server значение серверного сокета
        this.server = server;

        // Блок try ... catch ловим исключение
        try {
            // присваимаем полю socet значение клиентского сокета
            this.socket = socket;

            // определяем объекты ввода вывода информации
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            // ловим исключение
        } catch (IOException e) {

            // выводим сообщение в консоль
            System.out.println("Client handler initialization failed: " + e.getLocalizedMessage());
        }
    }

    // Переопределенный метод. Так как класс унаследован он класса Thread
    @Override
    public void run() {
        // блок try...catch который ловит исключения
        try {
            //цикл. пока клиентский сокет подключен и сокет не закрыт
            // isConnected() Returns the connection state of the socket.
            // isClosed() Returns the closed state of the socket.
            while (socket.isConnected() && !socket.isClosed()) {

                // получаем информацию из входящего потока данных
                // String readUTF(): считывает из потока строку в кодировке UTF-8
                String msg = in.readUTF();

                // условие. если сообщение начинается с команды авторизации
                // Не совсем понятно как в данном случае работает метод getText
                if (msg.startsWith(Command.AUTH_COMMAND.getText())) {

                    // то вызвать метод авторизации пользователя и передать в него полученное сообщение
                    if (!isAuth) userAuth(msg);

                    // иначе условие: если пользователь авторизован
                } else if (isAuth) {

                    //то, условие: сообщение начинается со спецсимвола
                    if (msg.startsWith("/")) {

                        // то, условие: если сообщение начинается с комманды "/w"
                        if (msg.startsWith(Command.PRIVATE_MESSAGE.getText() + " ")) {

                            // то, отправить сообщение конкретному пользователю
                            sendPrivateMessage(msg);

                            // иначе, условие: если сообщение начинается с комманды "/clients"
                        } else if (msg.startsWith(Command.CHAT_MESSAGE.getText() + " ")) {

                            // то , отправить сообщение в определенный чат
                            sendChatMessage(msg);
                        }
                        // если пользователь авторизован, но сообщение не содержит команды, то
                    } else {

                        // отправить сообщение в общий чат
                        sendBroadcastMessage(name + " написал: " + msg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected");
    }

    // Метод который по нику проверяет существует ли пользователь.
    // Метод получает от объекта сервер объект класса AuthService и вызывает
    // у него метод contains() , которуму передает ник полученный в качестве аргумента методу
    private boolean isUserExist(String userName) {
        return server.getAuthService().contains(userName);
    }

    // Метод отправляет персональное сообщение. В качестве аргумента принимает ник получателя и сообщение
    private void sendPersonalMessage(String user, String message) {
        server.sendPrivateMessage(name, user, message);
    }

    // Метод отправляет сообщение все подключенным клиентам
    private void sendBroadcastMessage(String msg) {
        server.sendBroadcastMessage(msg);
    }

    //метод отправляет сообщение в исходящем потоке данных.
    void sendMessage(String msg) {
        // Выводим сообщение в консоль
        System.out.println(name + ": " + msg);
        try {
            // Отправляем сообщение в исходящем потоке данных.
            out.writeUTF(msg);
            // ловим исключение
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод возвращает значение которое хранится в поле isAuth
    boolean isActive() {
        return isAuth;
    }

    // метод возвращет клиентский сокет
    Socket getSocket() {
        return socket;
    }

    // метод возвращает значение поля name
    String getHandlerName() {
        return name;
    }

    // метод отправляет сообщение в определенный чат
    private void sendChatMessage(String msg) {
        // /clients nick1     nick2   nick3 -m hello nick1
        // Метод substring() в Java имеет два варианта и возвращает новую строку,
        // которая является подстрокой данной строки. Подстрока начинается с символа,
        // заданного индексом, и продолжается до конца данной строки или до endIndex-1, если введен второй аргумент.
        // Метод split в Java разделяет строку на подстроки, используя разделитель, который определяется с помощью регулярного выражения.
        String[] data = msg.substring(Command.CHAT_MESSAGE.getText().length() + 1).split(" -m ");

        // условие, если длинна массива равна двум
        if (data.length == 2) {

            // то создается переменная типа строка, которой присваивается значение второй ячейки строкового массива data
            String message = data[1];

            // Создаем массив стрингов nicknames которому присвоим значения которые вернет метод split массиdа data
            String[] nicknames = data[0].split(" ");

            // Создаем новую коллекцию
            List<String> validUsers = new ArrayList<>();

            // Цикл. Который перебирает все значения массива nicknames
            for (String nickname : nicknames) {

                // Условие: если nickname не пустое значение то выполнить
                if (!nickname.trim().isEmpty()) {
                    // Условие: если пользователь подключен к серверу, то
                    if (isUserExist(nickname)) {
                        // добавить ник в коллекцию
                        validUsers.add(nickname);
                        // иначе
                    } else {
                        // отправить сообщение
                        sendMessage("Invalid username " + nickname);

                        // отправить сообщение в консоль
                        System.out.println("Invalid username " + nickname);
                    }
                }
            }

            // Лямбда выражение. А у меня с ними туго. Думаю логика такая, коллекция перебирает все элементы, и по никнейму отправляет сообщениея
            validUsers.forEach(username -> sendPersonalMessage(username, message + " " + validUsers.toString()));

            // Вызываем метод которому в качестве аргумента передаем сообщение и
            sendMessage(message + " for users: " + validUsers.toString());

        } else {
            // вызываем метод отправки сообщения
            sendMessage("Invalid chat message command!");
            // выводим сообщение в консоль
            System.out.println("Invalid chat message command!");
        }
    }

    // метод авторизации пользователя
    private void userAuth(String msg) {

        // Метод split в Java разделяет строку на подстроки, используя разделитель,
        // который определяется с помощью регулярного выражения.
        String[] data = msg.split(" ");

        // Условие: если длинна массива равна трем, то выполнить
        if (data.length == 3) {

            // полю name присваиваем значение полученное с сервера
            name = server.getAuthService().getNick(data[1], data[2]);

            // условие: если поле name не равно null
            if (name != null) {

                //отправить сообщение
                sendMessage("/authok " + name);

                // присвоить полю isAuth значение true
                isAuth = true;

                // отправить сообщение всем пользователям
                sendBroadcastMessage(name + " зашел в чат!");

                // иначе
            } else {

                // отправить сообщение
                sendMessage("Неверные логин/пароль");
            }
        }
    }

    // отправка приватного сообщениея
    private void sendPrivateMessage(String msg) {
        // разбить сообщение на массив строк
        String[] data = msg.substring(3).split(" ", 2);

        // присвоить переменной userName значение первого элемента массива
        String userName = data[0];

        // Условие: проверка есть ли пользователь в подключенных на сервере
        if (isUserExist(userName)) {
            // отправить сообщение
            sendMessage("я написал лично " + userName + ": " + data[1]);
            // отправить сообщение персонально пользователю по нику
            sendPersonalMessage(userName, data[1]);

        } else {
            // иначе вывести сообщение что такого пользователя не существует
            sendMessage("Попытка написать несуществующему пользователю "
                    + userName);
        }
    }

    // метод возвращает значение поля connectTime
    LocalDateTime getConnectTime() {
        return connectTime;
    }

    // переопределенный метод закрытия сокета
    @Override
    public void close() throws IOException {
        socket.close();
    }
}
