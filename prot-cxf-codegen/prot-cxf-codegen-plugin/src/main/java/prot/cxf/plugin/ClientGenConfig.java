package prot.cxf.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClientGenConfig {

    private Sei sei;
    private Map<String, List<String>> operations;

    public Sei getSei() {
        return sei;
    }

    public void setSei(Sei sei) {
        this.sei = sei;
    }

    public Map<String, List<String>> getOperations() {
        if (operations == null) {
            return Collections.emptyMap();
        }
        return operations;
    }

    public void setOperations(Map<String, List<String>> operations) {
        this.operations = operations;
    }

    public List<String> getSeiAnnotations() {
        if (sei == null || sei.getAnnotations() == null) {
            return List.of();
        }
        return sei.getAnnotations();
    }

    public static class Sei {
        private List<String> annotations;

        public List<String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<String> annotations) {
            this.annotations = annotations;
        }
    }
}

