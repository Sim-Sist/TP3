package output.logging;

import java.util.Iterator;

public class ProgressBar implements Iterator<Void> {
    private static int BAR_LENGTH = 15;
    private static final String FORMAT = "\t%3d%%: [%s]\r";
    private final String BAR_TOKEN = "=";
    private final String EMPTY_TOKEN = " ";
    private int step = 0;
    private int max;
    private boolean active = true;

    public ProgressBar(int maxValue) {
        this.max = maxValue - 1;
    }

    @Override
    public boolean hasNext() {
        return step <= max;
    }

    @Override
    public Void next() {
        step();
        return (null);
    }

    public void abort() {
        if (!active) {
            return;
        }
        System.out.print(ConsoleColors.RED_BOLD_BRIGHT);
        System.out.println("ABORTED");
        System.out.print(ConsoleColors.RESET);

        finish();
    }

    private void updateScreen() {
        StringBuilder bar = new StringBuilder();
        double progress = step / (double) max;
        int barProgress = (int) (progress * BAR_LENGTH);
        bar.append(ConsoleColors.GREEN_BRIGHT).append(BAR_TOKEN.repeat(barProgress)).append(ConsoleColors.RESET)
                .append(EMPTY_TOKEN.repeat(BAR_LENGTH - barProgress));
        // Format: percentage%: [bar]
        System.out.print(String.format(FORMAT, (int) (progress * 100), bar.toString()));
        if (step == max) {
            finish();
        }
    }

    private void step() {
        updateScreen();
        step++;
    }

    private void finish() {
        step = max + 1;
        active = false;
        System.out.println();
        System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT);
        System.out.println("done\n");
        System.out.print(ConsoleColors.RESET);
        Logger.progressBarFinished();
    }
}
