package cf.javadev.xyzreader.remote;

import java.net.MalformedURLException;
import java.net.URL;

class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {
            url = new URL("https://raw.githubusercontent.com/TNTest/xyzreader/master/data.json" );
        } catch (MalformedURLException ignored) {
            ignored.printStackTrace();
        }
        BASE_URL = url;
    }
}
