package javalibs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtils {
    private static volatile NetworkUtils _instance;
    public static final String BAD_NETWORK_ATTEMPT = "NETWORK_TIMEOUT";
    private final TSL log = TSL.get();
    private final Logic logic = Logic.get();

    private NetworkUtils() { }

    public static NetworkUtils get() {
        if(_instance == null) {
            synchronized (NetworkUtils.class) {
                if(_instance == null) {
                    _instance = new NetworkUtils();
                }
            }
        }
        return _instance;
    }

    /**
     * Write a message to some host without any expected response from the host
     * @param host The host to connect with as a URL string
     * @param port The port to connect to on the host
     * @param message The message to send to the host, must not be null
     */
    public void writeWithoutResponse(String host, int port, String message) {
        logic.require(message != null);

        try(Socket sock = new Socket(host, port);
            DataOutputStream writer = new DataOutputStream(sock.getOutputStream())){
            writer.writeBytes(message + "\n");
            writer.flush();
        }
        catch(IOException e){
            log.err("Unable to write to " + host);
            log.exception(e);
        }
    }

    /**
     * Attempts to ping a host on a specific port with a specific timeout
     * @param host The host to ping
     * @param port The port on the host
     * @param timeout How long to wait for a successful ping, in milliseconds
     * @return True if the host responds, else false
     */
    public boolean pingHost(String host, int port, int timeout){
        try(Socket sock = new Socket()){
            sock.connect(new InetSocketAddress(host, port), timeout);
            log.trace(host + " appears to be up.");
            return true;
        }
        catch(IOException isDown){
            log.info(host + " appears to be down.");
            return false;
        }
    }

    /**
     * Attempts to ping a web server on port 80 with a timeout of 1000ms
     * @param host The host to ping
     * @return True if host responds, else false
     */
    public boolean pingWebHost(String host) {
        return pingHost(host, 80, 1000);
    }

    /**
     * Send an error message to a host + port to be logged by that host
     * @param errorMsg The error message to be sent
     * @return True if server acknowledges error, else false
     */
    public boolean reportError(String errorMsg){
        throw new UnsupportedOperationException("reportError is not yet implemented");
    }

    /**
     * Get the external IP address when available
     * @return The external IP address if available, else BAD_NETWORK_ATTEMPT
     */
    public String externalIPAddr() {
        int timeoutMillis = 2500;
        String ipServiceURL = "http://whatismyip.akamai.com/";
        try{
            URL ipChecker = new URL(ipServiceURL);
            URLConnection urlConn = ipChecker.openConnection();
            urlConn.setConnectTimeout(timeoutMillis);
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConn.getInputStream()))){
                String ip = in.readLine();
                return ip != null ? ip : BAD_NETWORK_ATTEMPT;
            }
        }
        catch(Exception e){
            log.exception(e);
            return BAD_NETWORK_ATTEMPT;
        }
    }
}
