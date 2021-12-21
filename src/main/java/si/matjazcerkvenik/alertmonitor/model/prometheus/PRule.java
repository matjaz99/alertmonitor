package si.matjazcerkvenik.alertmonitor.model.prometheus;

import java.util.Map;

public class PRule {

    private String state;
    private String name;
    private String query;
    private String duration;
    private Map<String, String> labels;
    private Map<String, String> annotations;
    private String health;
    private Double evaluationTime;
    private String lastEvaluation;
    private String type;

}
