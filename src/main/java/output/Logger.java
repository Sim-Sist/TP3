package output;

public class Logger extends OutputManager {
    private int fileSteps = 0;
    private static String OUTPUT_LOGS_FILE_PATH = "/logs/";

    private String name;

    public Logger(String name) {
        this.name = name;
    }

    public void log(String msg) {
        System.out.println(String.format("->%s:\n %s", name.toUpperCase(), msg));
    }

    public boolean logFile(String content) {
        return outputDynamicFile(fileSteps++, getFilename(), content, OUTPUT_LOGS_FILE_PATH);
    }

    private String getFilename() {
        return String.format("%s-logs.txt", name);
    }

}
