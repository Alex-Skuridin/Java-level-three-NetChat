import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class TestFile {

    public static void main(String[] args) {

        File testFile = new File("Test_file.txt");
        FileWriter fileWriter = null;

        try {
            testFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
           fileWriter = new FileWriter("Test_file.txt", true);
            fileWriter.write("Hhfdsfmnbuhsf"  + ": [" + new Date().toString() + "]"+  System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }



}
