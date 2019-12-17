package javalibs;

public class Timer {
    private long start = 0;
    private long stop = 0;
    private long pauseStart = 0;
    private long pauseStop = 0;
    private long totalPausedTime = 0;

    /**
     * Start the timer
     */
    public void startTimer() {
        this.start = System.nanoTime();
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
        this.stop = System.nanoTime();
    }

    /**
     * Pause a timer, time while paused will not count against total execution time
     */
    public void pauseTimer() {
        this.pauseStart = System.nanoTime();
    }

    /**
     * Resume a timer after pausing the timer
     */
    public void resumeTimer() {
        this.pauseStop = System.nanoTime();
        long pausedNano = this.pauseStop - this.pauseStart;
        this.totalPausedTime += pausedNano;
    }

    /**
     * Get the total execution time in nanoseconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long getTotalPausedTimeNano() {
        return this.totalPausedTime;
    }

    /**
     * Get the total execution time in seconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long seconds() {
        long duration = totalElapsedNano() / 1_000_000_000;
        // If paused nanu < 1 second this will be 0, that is intentional
        long paused = getTotalPausedTimeNano() / 1_000_000_000;
        return duration - paused;
    }

    /**
     * Get the total execution time in milliseconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long milliseconds() {
        long duration = totalElapsedNano() / 1_000_000;
        // If paused nano < 1 millisecond this will be 0, that is intentional
        long paused = getTotalPausedTimeNano() / 1_000_000;
        return duration - paused;
    }

    /**
     * Get the total execution time in microeconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long microseconds() {
        long duration = totalElapsedNano() / 1000;
        // If paused nano < 1 microsecond this will be 0, that is intentional
        long paused = getTotalPausedTimeNano() / 1000;
        return duration - paused;
    }

    /**
     * Get the total execution time in nanoseconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long nanoseconds() {
        long durationNS = totalElapsedNano();
        return durationNS - getTotalPausedTimeNano();
    }

    private long totalElapsedNano() {
        return this.stop - this.start;
    }
}


/*
import java.util.concurrent.TimeUnit;

// Program to measure elapsed time in Java
class TimeUtil
{
	public static void main(String[] args) throws InterruptedException {

		long startTime = System.nanoTime();

		 ... the code being measured starts ...

// sleep for 5 seconds
		TimeUnit.SECONDS.sleep(5);

                 ... the code being measured ends ...

                long endTime = System.nanoTime();

                // get difference of two nanoTime values
                long timeElapsed = endTime - startTime;

                System.out.println("Execution time in nanoseconds  : " + timeElapsed);

                System.out.println("Execution time in milliseconds : " +
                timeElapsed / 1000000);
                }
                }
 */