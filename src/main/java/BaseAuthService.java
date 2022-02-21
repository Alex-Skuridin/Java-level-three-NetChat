import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    // Создаем вложенный класс
    public static class Entry {

        // поля класса
        private final int id;
        private final String login;
        private final String password;
        private final String nickname;

        // конструктор класса Entry
        public Entry(int id, String login, String password, String nickname) {
            this.id = id;
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }

        public int getId(){
            return id;
        }

        // возвращает значение поля login
        public String getLogin() {
            return login;
        }

        // возвращает значение поля password
        public String getPassword() {
            return password;
        }

        // возвращает значение поля nick
        public String getNickname() {
            return nickname;
        }
    }

    // Создаем переменную коллекции типа List, в которой будет хранится объекты класса Entry
    private List<Entry> entries;

    // Конструктор класса BaseAuthService
    public BaseAuthService() {

        // вызываем метод
        init();
    }

    //метод возвращает интовое значение равное размеру коллекции List<Entry> entries
    public int init() {
        // создаем коллекцию ArrayList и передаем ссылку в переменную entries
        entries = new ArrayList<>();

        try{
            //Создаем экземпляр по работе с БД
            DBHandler dbHandler = DBHandler.getInstance();
            // Добавляем запись
            // dbHandler.addProduct(new Product("Музей",200,"Развлечения"))
            // Получаем все записи и выводим на консоль
            this.entries = dbHandler.getAllUsers();

//            for (Entry entry : entries){
//                entries.add(new Entry(entry.getId(),entry.getLogin(), entry.getPassword(), entry.getNickname()));
//            }

            dbHandler.close();


        } catch (
                SQLException e){
            e.printStackTrace();
        }


        // добавляем в коллекцию новые объекты класса Entry

        // Возвращаем числовое значение размера коллекции
        return entries.size();



    }

    // переопределяем метод getNick который возвращает значение поля nick
    // в качестве аргумента передаются логин и пароль
    @Override
    public String getNick(String login, String pass) {
        // цикл который перебирает значения коллекции
        for (Entry e : entries) {
            // условие: если полученные логин и пароль равны тем что хранятся в полях объекта класса Entry, то
            // вызвать метод getNick
            if (e.getLogin().equals(login) && e.getPassword().equals(pass)) return e.getNickname();
        }
        // в противном случае вернуть null
        return null;
    }

    // метод который в качестве аргументов получает логин и пароль
    // и если они соответствуют тем что хранятся в полях объета
    // то возвращается булево значение истина
    @Override
    public boolean login(String login, String pass) { // не понятно применение метода
        for (Entry e : entries) {
            if (e.getLogin().equals(login) && e.getPassword().equals(pass)) return true;
        }
        return false;
    }

    //проверка существования никнейма
    @Override
    public boolean contains(String userName) {
        // если аргумент метода содержит null или пустое значение то вернуть false
        if (userName == null || userName.trim().isEmpty()) return false;

        // цикл по всем элементам коллекции
        for (Entry e : entries) {
            // Если нашлось похожее имя вернуть true
            if (userName.equals(e.getNickname())) return true;
        }
        return false;
    }

}
