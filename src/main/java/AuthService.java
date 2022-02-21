public interface AuthService {


    String getNick(String login, String pass);

    //метод выполнения авторизации
    boolean login(String login, String pass);

    //проверка существования никнейма
    boolean contains(String userName);
}
