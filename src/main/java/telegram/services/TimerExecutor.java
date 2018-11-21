package telegram.services;

import org.telegram.telegrambots.meta.logging.BotLogger;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 */
public class TimerExecutor {
    private static final String LOGTAG = "TIMEREXECUTOR";
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static volatile TimerExecutor instance;

    private TimerExecutor(){
    }

    /**
     * Singleton
     * @return instance of the executor
     */
    public static TimerExecutor getInstance(){
        final TimerExecutor currentInstance;
        if (instance == null){
            synchronized (TimerExecutor.class){
                if (instance == null){
                    instance = new TimerExecutor();
                }
                currentInstance = instance;
            }
        }
        else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    /**
     * Add a new CustomTimerTask to be executed
     * @param task Task to execute
     * @param targetHour Hour to execute
     * @param targetMin Min to execute
     * @param targetSec Sec to execute
     */
    public void startExecutionEveryDayAt(CustomTimerTask task, int targetHour, int targetMin, int targetSec){
        BotLogger.warn(LOGTAG, "Post new task" + task.getTaskName());
        final Runnable taskWrapper = () -> {
            try {
                task.execute();
                task.reduceTimes();
                startExecutionEveryDayAt(task, targetHour, targetMin, targetSec);
            }
            catch (Exception ex){
                BotLogger.severe(LOGTAG, "Unexpected exception at TimeExecutor", ex);
            }
        };
        if (task.getTimes() != 0){
            final long delay = computeNextDelay(targetHour, targetMin, targetSec);
            executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * Find out next daily execution
     * @param targetHour Target hour
     * @param targetMin Target minute
     * @param targetSec Target second
     * @return time in second to wait
     */
    private long computeNextDelay(int targetHour, int targetMin, int targetSec){
        final LocalDateTime localNow = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime localNextTarget = localNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        while (localNow.compareTo(localNextTarget.minusSeconds(1)) > 0){
            localNextTarget = localNextTarget.plusDays(1);
        }

        final Duration duration = Duration.between(localNow, localNextTarget);
        return duration.getSeconds();
    }

    @Override
    public void finalize(){
        this.stop();
    }

    private void stop(){
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        }
        catch (InterruptedException ex){
            BotLogger.severe(LOGTAG, ex);
        }
        catch (Exception ex){
            BotLogger.severe(LOGTAG, "Unexpected exception at TimeExecutor in second catch" ,ex);
        }
    }
}
