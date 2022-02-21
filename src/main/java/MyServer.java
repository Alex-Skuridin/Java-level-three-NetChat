

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// Этот класс отвечает за создание сервера, серверного подключения и его работу
public class MyServer {

    // Создаем переменную в которой определяем максимальное время задержки перед отключением клиента
    private static final long MAX_DELAY_TIME = 120; //Почему long ??????

    // Метод synchronizedList () класса java.util.Collections используется для возврата
    // синхронизированного (поточно-ориентированного) списка, подкрепленного указанным списком.
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // Объявляем переменную интерфейса AuthService
    private AuthService authService;

    // Точка входа в программу
    public static void main(String[] args) {
        // Создаем анонимный объект класса MyServer, и в качестве аргумента
        // передаем ему объект класса BaseAuthService
        new MyServer(new BaseAuthService());
    }


    // Конструктор класса MyServer. Передаем в качестве аргумента объект
    // интерфейса AuthService
    private MyServer(AuthService authService) {

        //Присваиваем полю authService значение объекта BaseAuthSrvice
        this.authService = authService;

        // Создаем переменную класса Socket и присваиваем ей значение null
        Socket s = null;
        // Создаем переменную класса ServerSocket и присваиваем ей значение null
        ServerSocket server = null;
        // Блок try ... cath который ловит исключение
        try {
            // Создаем объект ServerSocket и присваиваем его переменной server
            server = new ServerSocket(8189);
            // Выводим в консоль сообщение
            System.out.println("Server created. Waiting for client...");
            // Запускаем метод который по таймауту отключаем клиента
            startKiller();
            // Бесконечный цикл
            while (true) {
                // accept() будет ждать пока
                // кто-нибудь не захочет подключиться
                s = server.accept();
                // для нового подключения создаем новый объект ClientHandler
                // который в качестве аргумента принимает объект серверного сокета
                // и объект клиентского сокета
                ClientHandler client = new ClientHandler(this, s);
                // Т.к. Класс ClientHandler унаследован от класа Thread, то он имеет
                // переопределенный метод run. Запускаем отдельный поток у объекта
                // client
                client.start();
                // Добавляем созданный объект клиентского подключения в коллекцию
                clients.add(client);
            }
            // ловим исключение
        } catch (IOException e) {
            e.printStackTrace();
            // блок который будет выполнен в любом случае
        } finally {
            // Блок try ... cath который ловит исключение
            try {
                // условие, если переменная содержит серверный сокет, то сокет будет закрыт
                if (server != null) server.close();
                // Вывести сообщение в консоль
                System.out.println("Server closed");
                // условие, если переменная содержит клиентский сокет, то сокет будет закрыт
                if (s != null) s.close();
                // ловим исключение
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод который закрывает не авторизированные клиентские подключения
    private void startKiller() {
        // Создаем новый анонимный поток
        new Thread(() -> {
            // Бесконечный цикл
            while (true) {
                // Блок try ... cath который ловит исключение
                try {
                    // Усыпить поток на 1000 милисекунд
                    Thread.sleep(1000);

                    // Создаем переменную в которой будем хранить значение времени.
                    // В данном случае получаем системное значение времени в текущий момент
                    LocalDateTime now = LocalDateTime.now();

                    // Дословно «Iterator» можно перевести как «переборщик».
                    // То есть это некая сущность, способная перебрать все элементы в коллекции.
                    // При этом она позволяет это сделать без вникания во внутреннюю структуру и устройство коллекций.
                    // Итак, iterator для List — самая распространенная имплементация.
                    // Итератор идет от начала коллекции к ее концу: смотрит есть ли в наличии следующий элемент
                    // и возвращает его, если таковой находится.
                    Iterator<ClientHandler> i = clients.iterator();

                    //boolean hasNext() — если в итерируемом объекте (пока что это Collection)
                    // остались еще значение — метод вернет true, если значения кончились false.
                    while (i.hasNext()) {

                        // E next() — возвращает следующий элемент коллекции (объекта).
                        // Если элементов больше нет (не было проверки hasNext(), а мы вызвали next(),
                        // достигнув конца коллекции), метод бросит NoSuchElementException.
                        ClientHandler client = i.next();

                        // Условие. Если клиент не активный и время с момента подключения больше 120 секунд.
                        // То выполняется блок кода после условия.
                        // Класс Duration представляет собой промежуток времени с точностью до наносекунды.
                        // Класс Duration служит для хранения продолжительности времени на основе секунд и наносекунд.
                        // getSeconds()	- Получение продолжительности в нужном формате
                        // public static Duration between(Temporal startInclusive, Temporal endExclusive)
                        // возвращает разность значений времени начальной даты и конечной даты
                        // getSeconds() вернет значение в секундах
                        if (!client.isActive()
                                // класс Duration описывает длительность небольшого периода времени
                                // between получает длительность между двумя временными объектами
                                // getSeconds возвращает количество секунд в этой продолжительности
                                && Duration.between(client.getConnectTime(), now).getSeconds() > MAX_DELAY_TIME) {
                            //Вывести сообщение в консоль
                            System.out.println("close unauthorized user");
                            // Вызвать метод close  у объекта сlient
                            client.close();
                            // void remove() — удалит элемент, который был в последний раз получен методом next()
                            i.remove();
                        }
                    }
                    // ловим исключение
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // запускаем поток
        }).start();
    }

    // Возвращает объект BaseAuthService который хранится в поле authService
    AuthService getAuthService() {
        return authService;
    }

    // Отправляет сообщение всем активным клиентам которые есть в коллекции clients
    // В качестве аргумента получает строку
    void sendBroadcastMessage(String msg) {
        for (ClientHandler c : clients) {
            if (c.isActive()) c.sendMessage(msg);
        }
    }

    // Отправляет сообщение определенному пользователю.
    // В качестве аргумента получаем ник отправителя, ник кому отправить, сообщение типа строка
    void sendPrivateMessage(String from, String userName, String message) {
        // цикл for each который перебирает все объекты коллекции clients
        for (ClientHandler c : clients) {
            // создаем переменную типа строка и присваиваем ей значение имени которое нам
            // возвращет метод getHandlerName объекта клиентского подключения
            String name = c.getHandlerName();
            // Если имя объекта совпадает с именем полученным в аргументе, и этот клиент активный,
            // то выполняется отправка сообщения.
            if (name.equals(userName) && c.isActive())
                c.sendMessage(from + " написал лично " + userName + ": " + message);
        }
    }
}
