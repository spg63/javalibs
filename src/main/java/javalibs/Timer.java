package javalibs;

public class Timer {
    private long start = 0;
    private long stop = 0;
    private long pauseStart = 0;
    private long pauseStop = 0;
    private long totalPausedTime = 0;
    private boolean started = false;
    private boolean stopped = false;
    private boolean paused = false;

    /**
     * Start the timer
     */
    public void startTimer() {
        this.start = System.nanoTime();
        this.started = true;
        this.stopped = false;
        this.stop = 0;
        this.totalPausedTime = 0;
        this.paused = false;
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
        if(!started) throw new IllegalStateException("Timer has not been started");
        if(paused) throw new IllegalStateException("Timer is paused, resume before stopping");
        this.stop = System.nanoTime();
        this.stopped = true;
    }

    /**
     * Pause a timer, time while paused will not count against total execution time
     */
    public void pauseTimer() {
        if(!started) throw new IllegalStateException("Timer has not been started");
        if(paused) throw new IllegalStateException("Timer is already paused");
        this.pauseStart = System.nanoTime();
        this.paused = true;
    }

    /**
     * Resume a timer after pausing the timer
     */
    public void resumeTimer() {
        if(!paused) throw new IllegalStateException("Timer is not paused");
        this.pauseStop = System.nanoTime();
        long pausedNano = this.pauseStop - this.pauseStart;
        this.totalPausedTime += pausedNano;
        this.paused = false;
    }

    /**
     * Get the total paused time in nanoseconds
     * @return Total paused time in nanoseconds
     */
    public long getTotalPausedTimeNano() {
        return this.totalPausedTime;
    }

    /**
     * Get the total execution time in seconds, excluding the paused time
     * @return Timer in seconds, excluding pause time
     */
    public long seconds() {
        requireStopped();
        return (totalElapsedNano() - getTotalPausedTimeNano()) / 1_000_000_000;
    }

    /**
     * Get the total execution time in milliseconds, excluding the paused time
     * @return Timer in milliseconds, excluding pause time
     */
    public long milliseconds() {
        requireStopped();
        return (totalElapsedNano() - getTotalPausedTimeNano()) / 1_000_000;
    }

    /**
     * Get the total execution time in microseconds, excluding the paused time
     * @return Timer in microseconds, excluding pause time
     */
    public long microseconds() {
        requireStopped();
        return (totalElapsedNano() - getTotalPausedTimeNano()) / 1000;
    }

    /**
     * Get the total execution time in nanoseconds, excluding the paused time
     * @return Timer in nanoseconds, excluding pause time
     */
    public long nanoseconds() {
        requireStopped();
        return totalElapsedNano() - getTotalPausedTimeNano();
    }

    private long totalElapsedNano() {
        return this.stop - this.start;
    }

    private void requireStopped() {
        if(!stopped) throw new IllegalStateException("Timer has not been stopped");
    }
}
