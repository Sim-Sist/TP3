package output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

public class OutputManager {
    private final String LOCAL_OUTPUT_PATH = "src/main/output/";

    private String getRoot() {
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        return (u.toString().replace("file:", "").replace("bin/", ""));
    }

    protected boolean outputStaticFile(String filename, String content) {
        FileWriter fw;
        try {
            String filepath = getRoot() + LOCAL_OUTPUT_PATH;
            File file = new File(filepath, filename);
            file.createNewFile();
            fw = new FileWriter(file);

            // Write
            fw.append(content);

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean outputDynamicFile(int step, String filename, String content) {
        return outputDynamicFile(step, filename, content, "");
    }

    protected boolean outputDynamicFile(int step, String filename, String content, String localOutputPath) {
        String path = getRoot() + LOCAL_OUTPUT_PATH + localOutputPath;
        File f = new File(path, filename);
        if (step == 0) {
            clearContent(f);
        }
        FileWriter fw;
        try {
            fw = new FileWriter(f, true);

            // Write
            fw.append(content);

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean clearContent(File f) {
        try {
            f.delete();
        } catch (SecurityException e) {

        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("There was an error while creating output file:\n");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}