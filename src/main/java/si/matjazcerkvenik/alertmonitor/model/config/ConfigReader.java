/*
   Copyright 2021 Matjaž Cerkvenik

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package si.matjazcerkvenik.alertmonitor.model.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import si.matjazcerkvenik.alertmonitor.providers.PrometheusDataProvider;
import si.matjazcerkvenik.alertmonitor.util.LogFactory;

import java.io.*;
import java.util.List;

public class ConfigReader {

    public static YamlConfig loadProvidersYaml(String path) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class), representer);
        File f = new File(path);
        if (!f.exists()) return null;
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(f);
            YamlConfig config = yaml.load(inputStream);
            LogFactory.getLogger().info("providers config loaded: " + f.getAbsolutePath());
            verifyConfigAndSetDefaults(config.getProviders());
            return config;
        } catch (FileNotFoundException e) {
            LogFactory.getLogger().warn("ConfigReader: no file providers.yml found at " +  path);
        } catch (ConfigException e) {
            LogFactory.getLogger().error("ConfigReader: Exception loading providers.yml: " + e.getMessage());
        } catch (Exception e) {
            LogFactory.getLogger().error("ConfigReader:", e);
        }
        return null;
    }

    /**
     * Check all parameters if they suit the selected provider type and set default values where needed.
     * @return true if config is valid
     */
    public static List<ProviderConfig> verifyConfigAndSetDefaults(List<ProviderConfig> configs) throws ConfigException {
        for (ProviderConfig pc : configs) {

            if (pc.getName() == null) pc.setName("Provider_" + pc.hashCode());
            if (pc.getUri() == null) throw new ConfigException("missing uri parameter");
            if (pc.getType().equalsIgnoreCase("prometheus")) {
                // TODO check params
                Object connTim = pc.getParams().get(PrometheusDataProvider.DP_PARAM_KEY_CLIENT_CONNECT_TIMEOUT_SEC);
                LogFactory.getLogger().info("connT: " + connTim.toString());
                if (connTim == null || connTim.toString().length() == 0) {
                    connTim = "10";
                    pc.setParam(PrometheusDataProvider.DP_PARAM_KEY_CLIENT_CONNECT_TIMEOUT_SEC, String.valueOf(connTim));
                    LogFactory.getLogger().info("default 10 set");
                } else {
                    // check if number
                }
            } else if (pc.getType().equalsIgnoreCase("eventlogger")) {

            } else {

            }

        }


        return configs;
    }

}
