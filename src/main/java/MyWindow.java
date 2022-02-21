import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Date;


// JFrame - это Java-класс, который расширяется классом Frame Java. JFrame рассматривается как главное окно.
// В JFrame могут быть добавлены различные элементы, такие как метки, текстовые поля, кнопки.
// Эти элементы в JFrame создают графический интерфейс пользователя.
public class MyWindow extends JFrame {

    // Поля класса в которых хранится адресс сервера и адресс порта для подключения
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;

    // Текстовое поле JTextField является самым простым компонентом и наиболее часто
    // встречающимся в пользовательских интерфейсах. Как правило, поле является однострочным
    // и служит для ввода текста. В библиотеке Swing имеется два текстовых поля. Первое,
    // представленное классом JTextField, позволяет вводить однострочный текст. Второе поле,
    // реализованное классом JPasswordField и унаследованное от поля JTextField, дает возможность
    // организовать ввод «закрытой» информации (чаще всего паролей), которая не должна напрямую отображаться на экране.
    private JTextField login = new JTextField("Login"); // поле ввода логина
    private JPasswordField password = new JPasswordField("Password"); // поле ввода пароля

    //Кнопки JButton кроме собственного внешнего вида не включают практически ничего уникального.
    private JButton authBtn = new JButton("Auth"); // кнопка авторизации

    private JTextField jtf; // текстовое поле ввода сообщения

    //Многострочное текстовое поле JTextArea предназначено для ввода простого неразмеченного
    // различными атрибутами текста. В отличие от обычных полей, позволяющих вводить только
    // одну строку текста, многострочные поля дают пользователю возможность вводить произвольное количество строк текста.
    private JTextArea jta; // текстовое поле вывода сообщений

    // переменная сокет
    private Socket sock;

    // переменная входящего потока данных
    private DataInputStream in;

    // перпеменная исходящего потока данных
    private DataOutputStream out;

    // поле с переменной типа булево
    private boolean authorized = false;

    // Переменная хранящая объект для логирования чата
    private File logFile = null;

    // Переменная хранящая объект для записи данных в лог файл
    private FileWriter logFileWriter = null;

    //Переменная хранящая объект для чтения данных из лог файла
    private FileReader logFileReader = null;

    private Date date = null;


    // метод для входа в приложение
    public static void main(String[] args) {

        // создаем объект класса MyWindow и так как он унаследован от JFrame вызываем у него метод setVisible с аргументом true
        new MyWindow().setVisible(true);
    }

    // Конструктор класса
    private MyWindow() {
        //вызываем метод который инициализирует пользовательский интерфейс
        initUI();
        initLog();

    }

    // метод который создает пользовательский интерфейс
    private void initUI() {
        // устанавливаем размер окна
        setBounds(600, 300, 500, 500);

        // Заголовок окна
        setTitle("Client");

        // определяем операцию закрытия по умолчанию
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // метод который создает панель авторизации
        createAuthPanel();

        // Создаем новый объект JTextArea
        jta = new JTextArea();

        // устанавливаем свойство которое запрещает редактировать текст в объекте JTextArea
        jta.setEditable(false);

        // Метод позволяет переносить текст на новую строку
        jta.setLineWrap(true);

        // JScrollPane позволяет прокручивать содержимое визуального компонента и легко перемещаться к любой другой его части.
        // в качестве аргумента принимает объект JTextArea
        JScrollPane jsp = new JScrollPane(jta);
        // BorderLayout – это один из layout Java Swing, который используется для расположения
        // компонентов на контейнере. Принцип, который использует BorderLayout для компоновки
        // прост – всё пространство контейнера разбивается на пять частей. В каждой из этих
        // частей располагается один компонент. При добавлении компонента на контейнер с
        // BorderLayout разработчик обязательно указывает, куда именно он хочет поместить компонент.
        add(jsp, BorderLayout.CENTER);
        // JPanel, часть пакета Java Swing, представляет собой контейнер, который может хранить
        // группу компонентов. Основная задача JPanel — организовать компоненты, в JPanel можно
        // настроить различные макеты, которые обеспечивают лучшую организацию компонентов,
        // однако у него нет строки заголовка.
        // JPanel (LayoutManager l) : создает новый JPanel с указанным layoutManager
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // Добавляем панель в нижнюю часть окна
        add(bottomPanel, BorderLayout.SOUTH);
        // Создаем новый объект кнопки
        JButton jbSend = new JButton("SEND");
        // добавляем кнопку на нижнюю панель
        bottomPanel.add(jbSend, BorderLayout.EAST);
        // создаем объект поля ввода текства JTextField
        jtf = new JTextField();
        // Устанавливаем свойство объекта JTextField. Делаем его не активным.
        jtf.setEnabled(false);
        // помещаем поле ввода текста на нижнюю панель
        bottomPanel.add(jtf, BorderLayout.CENTER);

        // добавляем слушателя для кнопки отправки сообщений, опять лямбда, мать ее за ногу
        jbSend.addActionListener(e -> sendMsgFromUI());
        // добавляем слушателя для поля ввода текста, и тут тоже лямда, от такого количества сахара у меня будет диабет
        jtf.addActionListener(e -> sendMsgFromUI());

        // Следуя философии Java Swing нам нужно отыскать интерфейс нужного слушателя listener’а,
        // который может прослушивать события такого вида. Таким интерфейсом слушателя является
        // WindowListener. Этот интерфейс расположен в пакете java.awt.event и имеет несколько методов.
        // Методы рассмотрим чуть ниже. Далее мы должны реализовать этот интерфейс и полученного слушателя добавить к JFrame.
        // Как только слушатель создан, можно приступить к его добавлению к JFrame. Делается это при помощи методы addWindowListener.
        // В качестве параметра передается ссылка на экземпляр слушателя, реализующего WindowListener.
        // Абстрактный класс WindowAdapter используется для приема и обработки событий окна при создании объекта прослушивания.
        // Класс содержит методы: windowActivated(WindowEvent e), вызываемый при активизации окна; windowСlosing(WindowEvent e),
        // вызываемый при закрытии окна, и др.
        addWindowListener(new WindowAdapter() {
            // При наступлении события закрытия окна, вызываем метод windowClosing в классе родителе
            @Override
            public void windowClosing(WindowEvent event) {
                super.windowClosing(event);
                try {
                    out.writeUTF("end");
                    out.toString();
                    out.close();
                    in.close();
                    sock.close();
                } catch (IOException e) {
                    System.out.println("something happened on closing");
                }
            }
        });
    }

    private void createAuthPanel() {
        JPanel authPanel = new JPanel(new GridLayout());
        authPanel.add(login);
        login.setToolTipText("enter your login");
        password.setToolTipText("enter your password");
        authPanel.add(password);
        authPanel.add(authBtn);
        add(authPanel, BorderLayout.NORTH);
        authBtn.addActionListener(e -> connect(login.getText(), String.valueOf(password.getPassword())));
    }

    private void connect(String login, String password) {

        if (login.trim().isEmpty() || password.trim().isEmpty()) {
            System.out.println("login or password is empty!");
            return;
        }

        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            out.writeUTF("/auth " + login + " " + password);
            out.flush();

            new Thread(() -> {
                try {
                    while (sock.isConnected() && !sock.isClosed()) {
                        Thread.sleep(100);
                        String msg = in.readUTF();
                        if (msg.startsWith(Command.AUTHOK_COMMAND.getText())) {
                            String nick = msg.substring(Command.AUTHOK_COMMAND.getText().length() + 1);
                            setTitle(nick + "'s client");
                            setAuthorized(true);
                        } else if (msg.startsWith(Command.DISCONNECTED.getText())) {
                            jta.append("Connection closed..=(");
                            setAuthorized(false);
                        } else if (isAuthorized()) {
                            if (msg.equalsIgnoreCase("end session")) break;
                            jta.append(msg + System.lineSeparator());
                            writeLogWithData(msg);
                        }
                    }
                    setAuthorized(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgFromUI() {
        String msg = jtf.getText();
        sendMsg(msg);
        jtf.setText("");
        jtf.grabFocus();
    }

    private void sendMsg(String msg) {
        if (!msg.trim().isEmpty()) {
            try {
                out.writeUTF(msg);
                out.flush();
               // writeLog(out.toString());
            } catch (IOException e) {
                System.out.println("Fail to send message: " + e.getLocalizedMessage());
                writeLog("Exception: " + "Fail to send message: " + e.getLocalizedMessage());
            }
        }
    }

    private void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        jtf.setEnabled(authorized);
    }

    private boolean isAuthorized() {
        return authorized;
    }


    private void initLog(){

        date = new Date();

        logFile = new File ("LogFile.txt");

        if (!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        try {
            logFileReader = new FileReader(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    private void initLogHistory(){

    }

    private void writeLog(String line){

        try {
            logFileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void writeLogWithData(String line){

        StringBuilder logMessage = new StringBuilder(line);
        logMessage.append(" :");
        logMessage.append(date);
        logMessage.append(System.lineSeparator());

        try {
            logFileWriter = new FileWriter(logFile,true);
            logFileWriter.write(logMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                logFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
