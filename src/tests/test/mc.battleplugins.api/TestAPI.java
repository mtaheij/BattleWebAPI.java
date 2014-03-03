package test.mc.battleplugins.api;


import junit.framework.TestCase;
import mc.battleplugins.api.BattlePluginsAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestAPI extends TestCase {
    String testDir = System.getProperty("user.home") +
            "/workspace/mc/BattlePluginsAPI/test_files";
    String configFile = testDir+"/TestConfig.yml";

    public void testPaste() throws IOException {
        BattlePluginsAPI api = new TestBattlePluginsAPI("TestServer", "1.0", configFile);
        api.pasteFile("TestAPI", testDir+"/TestPasteFile.txt");
    }

    public void testStats() throws IOException {
        TestBattlePluginsAPI api = new TestBattlePluginsAPI("TestServer", "1.0", configFile);
        api.setPlayersOnline(new Random().nextInt(10));
        TestPlugin tp = new TestPlugin("TestPlugin", "1.0");
        api.sendStatistics(tp);
    }

    Map<String,Object> stringToMap(String str) {
        Map<String,Object> map = new HashMap<String, Object>();
        int f = str.indexOf('{');
        int l = str.lastIndexOf('}');
        if (f == -1 || l == -1 || f >= l || str.length() < 2) {
            return map;}
        String[] kvs = str.substring(f+1, l).split(",");
        for (String kv : kvs) {
            String[] split = kv.replace('"', ' ').split(":");
            map.put(split[0].trim(), split[1].trim());
        }
        return map;
    }

    public void testParser() {
        String str = "{\"id1\":\"val1\", \"id2\" :\"val2\", \"id3\": \"val3\" , \"id4\" : \"val4\" ,"+
                " \"id5\":{\"sid1\":\"sval1\", \"sid2\":\"sval2\", \"sid3\":\"sval3\" , \"sid4\":\"sval4\"}}";
        Map<String, Object> result = stringToMap(str);
        assertEquals("val1",result.get("id1"));
        assertEquals("val2",result.get("id2"));
        assertEquals("val3",result.get("id3"));
        assertEquals("val4",result.get("id4"));
//        assertEquals("sval1", ((Map<String,Object>)result.get("id5")).get("sid1"));

    }
}
