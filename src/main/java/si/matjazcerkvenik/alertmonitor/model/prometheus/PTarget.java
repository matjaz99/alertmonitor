package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.Map;

public class PTarget {

    private Map<String, String> discoveredLabels;
    private Map<String, String> labels;
    private String scrapePool;
    private String scrapeUrl;
    private String globalUrl;
    private String lastError;
    private String lastScrape;
    private String lastScrapeDuration;
    private String health;
    private String scrapeInterval;
    private String scrapeTimeout;

    public Map<String, String> getDiscoveredLabels() {
        return discoveredLabels;
    }

    public void setDiscoveredLabels(Map<String, String> discoveredLabels) {
        this.discoveredLabels = discoveredLabels;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getScrapePool() {
        return scrapePool;
    }

    public void setScrapePool(String scrapePool) {
        this.scrapePool = scrapePool;
    }

    public String getScrapeUrl() {
        return scrapeUrl;
    }

    public void setScrapeUrl(String scrapeUrl) {
        this.scrapeUrl = scrapeUrl;
    }

    public String getGlobalUrl() {
        return globalUrl;
    }

    public void setGlobalUrl(String globalUrl) {
        this.globalUrl = globalUrl;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getLastScrape() {
        return lastScrape;
    }

    public void setLastScrape(String lastScrape) {
        this.lastScrape = lastScrape;
    }

    public String getLastScrapeDuration() {
        return lastScrapeDuration;
    }

    public void setLastScrapeDuration(String lastScrapeDuration) {
        this.lastScrapeDuration = lastScrapeDuration;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getScrapeInterval() {
        return scrapeInterval;
    }

    public void setScrapeInterval(String scrapeInterval) {
        this.scrapeInterval = scrapeInterval;
    }

    public String getScrapeTimeout() {
        return scrapeTimeout;
    }

    public void setScrapeTimeout(String scrapeTimeout) {
        this.scrapeTimeout = scrapeTimeout;
    }

    @Override
    public String toString() {
        return "PTarget{" +
                "discoveredLabels=" + discoveredLabels +
                ", labels=" + labels +
                ", scrapePool='" + scrapePool + '\'' +
                ", scrapeUrl='" + scrapeUrl + '\'' +
                ", globalUrl='" + globalUrl + '\'' +
                ", lastError='" + lastError + '\'' +
                ", lastScrape='" + lastScrape + '\'' +
                ", lastScrapeDuration='" + lastScrapeDuration + '\'' +
                ", health='" + health + '\'' +
                ", scrapeInterval='" + scrapeInterval + '\'' +
                ", scrapeTimeout='" + scrapeTimeout + '\'' +
                '}';
    }
}
