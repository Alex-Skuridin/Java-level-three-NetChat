import java.io.*;

public class ChatLog {

    private String FILENAME = "Log.txt";

    private File chatFile = null;

    private StringReader chatStringReader = null;

    private StringWriter chatStringWriter = null;

    private FileWriter logFileWriter = null;

    private DataInputStream dataInputStream = null;

    private DataOutputStream dataOutputStream = null;



    public ChatLog(){
        chatFile = new File(FILENAME);

        if (!chatFile.exists()){

            try {
                chatFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void writeLine() {

    }

    public void readLine(){

    }


}
