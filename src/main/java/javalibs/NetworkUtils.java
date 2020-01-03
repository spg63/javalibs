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
    private static volatile NetworkUtils instance_;
    public String BAD_NETWORK_ATEMPT = "NETWORK_TIMEOUT";
    private TSL log = TSL.get();
    private Logic logic = Logic.get();

    private NetworkUtils() { }

    public static NetworkUtils get() {
        if(instance_ == null) {
            synchronized (NetworkUtils.class) {
                if(instance_ == null) {
                    instance_ = new NetworkUtils();
                }
            }
        }
        return instance_;
    }

    /**
     * Write a message to some host without any expected response from the host
     * @param host The host to connect with as a URL string
     * @param port The port to connect to on the host
     * @param message The message to send to the host, must not be null
     */
    public void writeWithoutResponse(String host, int port, String message) {
        Socket sock = null;
        DataOutputStream writer = null;

        logic.require(message != null);

        try{
            sock = new Socket(host, port);
            writer = new DataOutputStream(sock.getOutputStream());
            writer.writeBytes(message + "\n");
            writer.flush();
        }
        catch(IOException e){
            log.err("Unable to write to " + host);
            log.exception(e);
        }
        finally {
            if(writer != null){
                try {
                    writer.close();
                }
                catch(IOException e){
                    log.exception(e);
                }
            }
            if(sock != null){
                try{
                    sock.close();
                }
                catch(IOException e){
                    log.exception(e);
                }
            }
        }
    }

    /**
     * Attempts to ping a host on a specific port with a specific timeout
     * @param host The host to ping
     * @param port The port on the host
     * @param timeout How long to wait for a successful ping
     * @return True if the host responds, else false
     */
    public boolean pingHost(String host, int port, int timeout){
        try{
            Socket sock = new Socket();
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
     * Send an error message to a host + port to be logged, probably, by that host
     * @param msg The error messae to be sent
     */

    /**
     * Get the external IP address when available
     * @return The external IP address if available else NETWORK_UNAVAILABLE
     */
    public String externalIPAddr() {
        URL ipChecker = null;
        BufferedReader in = null;
        String ip = null;
        URLConnection urlConn = null;
        int timeoutMillis = 2500;
        String awesomeIPGetterWebsiteThanksForTheHelp = "http://whatismyip.akamai.com/";
        try {
            ipChecker = new URL(awesomeIPGetterWebsiteThanksForTheHelp);
            urlConn = ipChecker.openConnection();
            urlConn.setConnectTimeout(timeoutMillis);
            in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            ip = in.readLine();
        }
        catch (Exception e) {
            log.exception(e);
            return BAD_NETWORK_ATEMPT;
        }
        finally {
            if(in != null) {
                try {
                    in.close();
                }
                catch(IOException e){
                    log.exception(e);
                }
            }
        }

        if(ip == null){
            return BAD_NETWORK_ATEMPT;
        }

        return ip;
    }
}
