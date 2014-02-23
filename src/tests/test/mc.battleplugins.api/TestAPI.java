package test.mc.battleplugins.api;


import junit.framework.TestCase;
import mc.battleplugins.api.BattlePluginsAPI;

import java.io.IOException;

public class TestAPI extends TestCase {
    String testDir = System.getProperty("user.home") +
            "/workspace/mc/BattlePluginsAPI/test_files";
    String configFile = testDir+"/TestConfig.yml";

    public void testPaste() throws IOException {
        BattlePluginsAPI api = new TestBattlePluginsAPI("TestServer", "1.0", configFile);
        api.pasteFile("TestAPI", testDir+"/TestPasteFile.txt");
    }

    public void testStats() throws IOException {
        BattlePluginsAPI api = new TestBattlePluginsAPI("TestServer", "1.0", configFile);
        TestPlugin tp = new TestPlugin("TestPlugin", "1.0");
        api.sendStatistics(tp);
    }
}
