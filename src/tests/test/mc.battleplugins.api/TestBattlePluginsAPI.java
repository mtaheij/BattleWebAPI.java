package test.mc.battleplugins.api;

import mc.battleplugins.api.BattlePluginsAPI;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author alkarin
 */
public class TestBattlePluginsAPI extends BattlePluginsAPI {
    String configPath;
    String name;
    String version;
    int playersOnline = 0;

    public TestBattlePluginsAPI(String testServer,String version, String configPath) throws IOException {
        super();
        this.name = testServer;
        this.version = version;
        this.configPath = configPath;
    }

    @Override
    public File getConfigurationFile() {
        return new File(configPath);
    }

    @Override
    public void addServerInfo() throws UnsupportedEncodingException {
        addPair("bServerName", name);
        addPair("bVersion", version);
        addPair("bOnlineMode", "true");
        addPair("bPlayersOnline", String.valueOf(playersOnline));
    }

    public void setPlayersOnline(int playersOnline) {
        this.playersOnline = playersOnline;
    }
}
