import org.sqlite.JDBC;

import java.sql.*;
import java.util.*;


public class DBHandler {


    // Константа в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:Chat.db";

    // Используем шаблон одиночка, чтобы не плодить множество
    // экземпляров класса DBHandler
    private static DBHandler instanse = null;

    public static synchronized DBHandler getInstance() throws SQLException {
        if (instanse == null) {
            instanse = new DBHandler();
        }
        return instanse;
    }


    //Объект, в котором будет храниться соединение с БД
    private Connection connection;

    private DBHandler() throws SQLException {
        // Регистрируем драйвер, с которым будет работать
        // в нашем случае SQLite
        DriverManager.registerDriver(new JDBC());
        //Выполняем подключение к базе данных
        this.connection = DriverManager.getConnection(CON_STR);
    }

    public List<BaseAuthService.Entry> getAllUsers() {
        //Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты полученные из БД
            List<BaseAuthService.Entry> entry = new ArrayList<BaseAuthService.Entry>();
            // В resultSet, будет храниться результат нашего запроса
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT id, login, password, nickname FROM Authorization");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                entry.add(new BaseAuthService.Entry(resultSet.getInt("id"),
                                                       resultSet.getString("login"),
                                                       resultSet.getString("password"),
                                                       resultSet.getString("nickname")));
            }
            // Возвращаем наш список
            return entry;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка, возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    public void addProduct(BaseAuthService.Entry entry) {
        // Создадим подготовленное приложение, что бы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Authorization(id,'login','password','nickname') " +
                        "VALUES(?,?,?,?)")) {
            statement.setObject(1, entry.getId());
            statement.setObject(2, entry.getLogin());
            statement.setObject(3, entry.getPassword());
            statement.setObject(4, entry.getNickname());
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление продукта по id
    public void deleteEntry (int id){
        try(PreparedStatement statement = this.connection.prepareStatement("DELETE FROM Authorization WHERE id = ?")){
            statement.setObject(1, id);
            //Выполняем запрос
            statement.execute();
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void close(){
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
