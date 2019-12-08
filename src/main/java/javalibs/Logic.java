package javalibs;

public class Logic {
    private TSL log_ = TSL.get();
    public static boolean ALLOW_REQUIRE = true;
    private static volatile Logic _instance;
    private Logic() { }

    public static Logic get() {
        if(_instance == null){
            synchronized (Logic.class){
                if(_instance == null){
                    _instance = new Logic();
                }
            }
        }
        return _instance;
    }

    /**
     * Similar to assertTrue, will kill the program if trueToLive is false, however it
     * kills it from the logger, automatically giving you function, line information.
     * Essentially the info from an exception while also letting the logger die properly
     * @param trueToLive True or false, false dies
     * @param msg Additional log message
     */
    public void require(Boolean trueToLive, Object msg) {
        if(!ALLOW_REQUIRE) return;
        log_.die(log_.getStackInfo(msg));
    }

    /**
     * Similar to assertTrue, will kill the program if trueToLive is false, however it
     * kills it from the logger, automatically giving you function, line information.
     * Essentially the info from an exception while also letting the logger die properly
     * @param trueToLive If false, kills program
     */
    public void require(Boolean trueToLive) {
        if(!ALLOW_REQUIRE) return;
        log_.die(log_.getStackInfo(""));
    }

    /**
     * Kills the program while reporting the location from where the program was killed
     * @param msg Message to be logged
     */
    public void dieFrom(Object msg) {
        log_.die(log_.getStackInfo(msg));
    }
}
