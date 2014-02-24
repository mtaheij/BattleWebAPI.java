package mc.battleplugins.api;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

/**
 * @author alkarin, lducks
 */
public class BattlePluginsAPI {
    /** current version */
    public static final long version = 1L;

    /** request protocol */
    static final String PROTOCOL = "http";

    /** battleplugins site */
    static final String HOST = "battleplugins.com";

    /** current user agent */
    static final String USER_AGENT = "BattlePluginsAPI/v1.0";

    /** api key: used to verify requests */
    protected String apiKey;

    /** whether to send stats or not */
    AtomicBoolean sendStats;

    /** ID of any currently running tasks */
    Integer timer;

    /** Key Value pairs to send */
    final Map<String, String> pairs;

    public BattlePluginsAPI() throws IOException {
        pairs = new TreeMap<String, String>();
    }

    private static String urlEncode(final String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }

    /**
     * Paste the given file to the battleplugins website
     * @param title Title of the paste
     * @param file Full path to the file to paste
     * @throws IOException
     */
    public void pasteFile(String title, String file) throws IOException {
        FileConfiguration c = getConfig();
        apiKey = c.getString("API-Key", null);

        if (apiKey == null) {
            throw new IOException("API Key was not found. You need to register before sending pastes");}
        File f = new File(file);
        addPair("title", title);
        addPair("content", toString(f.getPath()));
        post(new URL(PROTOCOL + "://" + HOST + "/api/web/paste/create"));
    }

    /**
     * Send statistics about the server and the given plugin
     * @param plugin the plugin to send information about
     * @throws IOException
     */
    public void sendStatistics(Plugin plugin) throws IOException {
        addPluginInfo(plugin);
        addServerInfo();
        addSystemInfo();
        post(new URL(PROTOCOL + "://" + HOST + "/statistics/set"));
    }

    /**
     * Add the given pair to request
     * @param key key to send
     * @param value value to send
     * @throws UnsupportedEncodingException
     */
    public void addPair(String key,String value) throws UnsupportedEncodingException {
        pairs.put(key, urlEncode((value)));
    }

    /**
     * Add the given pair to request, will zip the value
     * @param key key to send
     * @param value value to zip and send
     * @throws UnsupportedEncodingException
     */
    public void addZippedPair(String key,String value) throws IOException {
        pairs.put(key, gzip(value));
    }

    /**
     * Add plugin info to the request
     * @param plugin the plugin to send information about
     * @throws UnsupportedEncodingException
     */
    public void addPluginInfo(Plugin plugin) throws UnsupportedEncodingException {
        PluginDescriptionFile d = plugin.getDescription();
        addPair("pName", d.getName());
        addPair("pVersion", d.getVersion());
    }

    /**
     * Add the server info to this request
     * @throws UnsupportedEncodingException
     */
    public void addServerInfo() throws UnsupportedEncodingException {
        addPair("bServerName", Bukkit.getServerName());
        addPair("bVersion", Bukkit.getVersion());
        addPair("bOnlineMode", String.valueOf(Bukkit.getServer().getOnlineMode()));
        addPair("bPlayersOnline", String.valueOf(Bukkit.getServer().getOnlinePlayers().length));
    }

    /**
     * Add system info to this request
     * @throws UnsupportedEncodingException
     */
    public void addSystemInfo() throws UnsupportedEncodingException {
        addPair("osArch", System.getProperty("os.arch"));
        addPair("osName", System.getProperty("os.name"));
        addPair("osVersion", System.getProperty("os.version"));
        addPair("jVersion", System.getProperty("java.version"));
        addPair("nCores", String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * Send a get request
     * @param baseUrl url destination
     * @throws IOException
     */
    public void get(URL baseUrl) throws IOException {
        /// Connect
        URL url = new URL (baseUrl.getProtocol()+"://"+baseUrl.getHost()+baseUrl.getPath() + "?" + toString(pairs));
        URLConnection connection = url.openConnection(Proxy.NO_PROXY);

        /// Connection information
        connection.addRequestProperty("GET", "/api/web/blog/all HTTP/1.1");
        connection.addRequestProperty("Host", HOST);
        connection.addRequestProperty("X-API-Key", apiKey);
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);

        /// write the data to the stream
        OutputStream os = connection.getOutputStream();
        os.flush();

        /// Get our response
        final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null){
            System.out.println(line);
        }

        os.close();
        br.close();
    }

    /**
     * Send a post request
     * @param url destination
     * @throws IOException
     */
    public void post(URL url) throws IOException {
        /// Connect
        URLConnection connection = url.openConnection(Proxy.NO_PROXY);

        byte[] data = toString(pairs).getBytes();
        System.out.println(toString(pairs));
        /// Connection information
        connection.addRequestProperty("POST", "/api/web/blog/all HTTP/1.1");
        connection.addRequestProperty("Host", HOST);
        connection.addRequestProperty("X-API-Key", apiKey);
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Content-length",String.valueOf(data.length));
        connection.setDoOutput(true);

        /// write the data to the stream
        OutputStream os = connection.getOutputStream();
        os.write(data);
        os.flush();

        /// Get our response
        final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ( (line = br.readLine()) != null){
            System.out.println(line);
        }

        os.close();
        br.close();
    }

    String gzip(String str) throws IOException {
        GZIPOutputStream out = new GZIPOutputStream(new ByteArrayOutputStream());
        out.write(str.getBytes());
        out.close();
        return out.toString();
    }

    String toString(Map<String, String> pairs) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> e : pairs.entrySet()) {
            if (!first) sb.append("&");
            else first = false;
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }


    @Override
    public String toString(){
        return "[BattlePluginsAPI]";
    }

    /**
     * Get the configuration file
     * @return returns the configuration file
     * @throws IOException if file not found or could not be created
     */
    public File getConfigurationFile() throws IOException {
        File pluginFolder = Bukkit.getServer().getUpdateFolderFile().getParentFile();
        File f = new File(pluginFolder,"BattlePluginsAPI");
        if (!f.exists()){
            if (!f.mkdirs()){
                throw new IOException("Couldn't create config directory");}
        }
        try {
            f = new File(f, "config.yml");
            if (!f.exists()){
                if (f.createNewFile()) {
                    throw new IOException("Couldn't create config file");}
            }
            return f;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the configuration
     * @return returns the configuration
     * @throws IOException if file not found or could not be created
     */
    public FileConfiguration getConfig() throws IOException {
        File f = getConfigurationFile();

        FileConfiguration c;
        c = YamlConfiguration.loadConfiguration(f);
        if (c.get("API-Key", null) == null) {
            c.options().header("API-Key : unique id for website authentication");
            c.options().header("SendStatistics : set to false to not send statistics");
            c.addDefault("API-Key", "");
            c.addDefault("SendStatistics", true);
            c.options().copyDefaults(true);
            c.save(f);
        }
        return c;
    }

    /**
     * schedule BattlePluginsAPI to send plugin statistic
     * @param plugin the plugin to send information about
     */
    public void scheduleSendStats(final Plugin plugin) {
        if (!sendStats.get())
            return;
        if (timer != null){
            Bukkit.getScheduler().cancelTask(timer);}
        //noinspection deprecation
        timer = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable(){
            @Override
            public void run() {
                if (!sendStats.get())
                    return;
                try {
                    if (!sendStats.get()){
                        sendStatistics(plugin);
                    } else if (timer !=null){
                        Bukkit.getScheduler().cancelTask(timer);
                        timer = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    sendStats.set(false);
                }
            }
        },20*60, 20*60*30/*every 30 min*/);
    }

    /**
     * stop sending plugin statistic
     */
    public void stopSendingStatistics() throws IOException {
        setSending(false);
        if (timer != null) {
            Bukkit.getScheduler().cancelTask(timer);
            timer = null;
        }
    }

    /**
     * start sending plugin statistics
     * @param plugin the plugin to send information about
     * @throws IOException
     */
    public void startSendingStatistics(Plugin plugin) throws IOException {
        setSending(true);
        scheduleSendStats(plugin);
    }

    private void setSending(boolean send) throws IOException {
        sendStats.set(send);
        FileConfiguration config = getConfig();
        if (config.getBoolean("SendStatistics", !send)){
            config.set("SendStatistics", send);
            config.save(getConfigurationFile());
        }
    }

    /** Code from erikson, http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file*/
    private static String toString(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
