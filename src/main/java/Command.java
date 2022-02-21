
public enum Command {
    AUTH_COMMAND("/auth"), // "/auth" - аргумент конструктора
    AUTHOK_COMMAND("/authok"),
    DISCONNECTED("/disconnected"),
    PRIVATE_MESSAGE("/w"),
    CHAT_MESSAGE("/clients");

    // метод который возвращает значение поля text
    public String getText() {
        return text;
    }

    // поле объекта класса перечисления
    private String text;

    // Конструктор перечисления
    Command(String s) {
        text = s;
    }
}
