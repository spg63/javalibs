/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/1/15
 */
@SuppressWarnings("unused")
public class StateVariables_sg{
    private StateVariables_sg(){}

    /**
     * Internal variable used when I'm checking the code of the classes
     * aggregated by UserUtils, as well as DirWatcher + FileHasher
     */
    public static boolean debug_lib = false;

    /**
     * Print extra messages
     */
    public static boolean verbose = true;

    /**
     * Add time stamps to log / errLog messages
     */
    public static boolean log_time_stamps = false;
    public static boolean err_time_stamps = true;

    /**
     * Disable debug print / log messages
     */
    public static boolean enable_debug = true;

}
