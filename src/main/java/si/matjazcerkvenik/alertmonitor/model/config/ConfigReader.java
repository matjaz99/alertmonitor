package si.matjazcerkvenik.alertmonitor.model.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.io.*;

public class ConfigReader {

    public static void main(String... args) {
        ConfigReader.loadYaml("providers.yml");
    }

    public static YamlConfig loadYaml(String path) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class), representer);
        File f = new File(path);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
            YamlConfig config = yaml.load(inputStream);
            LogFactory.getLogger().info("providers config loaded: " + config.toString());
            return config;
        } catch (FileNotFoundException e) {
            LogFactory.getLogger().warn("no providers.yml found at " +  path);
        } catch (Exception e) {
            LogFactory.getLogger().error("Exception reading providers.yml", e);
        }
        return null;
    }

}
