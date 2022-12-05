package si.matjazcerkvenik.alertmonitor.model.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.io.*;

public class ConfigReader {

    public static void main(String... args) {
        ConfigReader.loadYaml("alertmonitor-config.yml");
    }

    public static Config loadYaml(String path) {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        File f = new File(path);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            LogFactory.getLogger().warn("no alertmonitor-config.yml found at " +  path);
            return null;
        }
        Config config = yaml.load(inputStream);
        LogFactory.getLogger().info("config loaded: " + config.toString());
        return config;
    }

}
