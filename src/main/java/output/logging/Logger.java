package output.logging;

import output.OutputManager;

public class Logger extends OutputManager {
    private int fileSteps = 0;
    private static String OUTPUT_LOGS_FILE_PATH = "/logs/";
    private static boolean _progressBarIsActive = false;
    private static StringBuilder bufferedLogs = new StringBuilder();

    private String name;

    public Logger(String name) {
        this.name = name;
    }

    public void log(String msg) {
        String output = String.format("->%s:\n %s", name.toUpperCase(), msg);

        // ProgressBar hijacks the console
        if (progressBarIsActive()) {
            bufferedLogs.append(output).append('\n');
            return;
        }
        System.out.println(output);
    }

    public boolean logFile(String content) {
        return outputDynamicFile(fileSteps++, getFilename(), content, OUTPUT_LOGS_FILE_PATH);
    }

    private String getFilename() {
        return String.format("%s-logs.txt", name);
    }

    public ProgressBar progressBar(int max) throws Exception {
        if (progressBarIsActive()) {
            throw new Exception();
        }
        _progressBarIsActive = true;

        return new ProgressBar(max);
    }

    protected static void progressBarFinished() {
        _progressBarIsActive = false;
        System.out.println(bufferedLogs.toString());
        bufferedLogs.delete(0, bufferedLogs.length());
    }

    public static boolean progressBarIsActive() {
        return _progressBarIsActive;
    }
}
