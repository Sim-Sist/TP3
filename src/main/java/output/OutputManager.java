package main.java.output;

import java.net.URL;

public class OutputManager {
    final String LOCAL_OUTPUT_PATH = "src/main/output/";

    String getRoot() {
        URL u = getClass().getProtectionDomain().getCodeSource().getLocation();
        return (u.toString().replace("file:", "").replace("bin/", ""));
    }
}