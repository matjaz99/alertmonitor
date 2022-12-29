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
import si.matjazcerkvenik.alertmonitor.util.MD5;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {

    public static YamlConfig loadProvidersYaml(String path) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class), representer);
        File f = new File(path);
        try {
            if (!f.exists()) throw new FileNotFoundException();
            InputStream inputStream = new FileInputStream(f);
            YamlConfig config = yaml.load(inputStream);
            LogFactory.getLogger().info("providers config loaded: " + f.getAbsolutePath());
            verifyConfigs(config.getProviders());
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
    public static List<ProviderConfig> verifyConfigs(List<ProviderConfig> configs) throws ConfigException {

        if (configs == null) throw new ConfigException("config is null");

        for (ProviderConfig pc : configs) {

            // check mandatory parameters
            LogFactory.getLogger().info("checking provider config: " + pc.getName());
            if (pc.getName() == null) throw new ConfigException("missing provider name");
            pc.setId(MD5.getChecksum(pc.getName()));
            if (pc.getUri() == null) throw new ConfigException("missing provider uri");
            if (pc.getType() == null) throw new ConfigException("missing provider type");

            // add empty map if whole params section is missing, it will be filled below
            if (pc.getParams() == null) pc.setParams(new HashMap<>());

            if (pc.getType().equalsIgnoreCase("prometheus")) {

                checkParam(pc.getParams(), PrometheusDataProvider.DP_PARAM_KEY_SERVER, "http://undefined-hostname-config:9090");
                checkParam(pc.getParams(), PrometheusDataProvider.DP_PARAM_KEY_CLIENT_POOL_SIZE, "1");
                checkParam(pc.getParams(), PrometheusDataProvider.DP_PARAM_KEY_CLIENT_CONNECT_TIMEOUT_SEC, "10");
                checkParam(pc.getParams(), PrometheusDataProvider.DP_PARAM_KEY_CLIENT_READ_TIMEOUT_SEC, "60");
                checkParam(pc.getParams(), PrometheusDataProvider.DP_PARAM_KEY_SYNC_INTERVAL_SEC, "60");

            } else if (pc.getType().equalsIgnoreCase("eventlogger")) {

            } else {

            }

        }


        return configs;
    }

    /**
     * Check if param exists. If not, then fill it with default value.
     * @param params
     * @param defaultValue
     */
    private static void checkParam(Map<String, Object> params, String paramName, String defaultValue) {
        Object p = params.get(paramName);
        if (p == null) {
            params.put(paramName, defaultValue);
            LogFactory.getLogger().warn("param " + paramName + " is missing; default will be used: " + defaultValue);
        }
    }

}
