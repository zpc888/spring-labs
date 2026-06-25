package prot.cxf.plugin;

public class StaticHeader {

    private String name;
    private String value;
    private Boolean ifExisting;     // Overwrite, Merge, Discard

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getIfExisting() {
        return ifExisting;
    }

    public void setIfExisting(Boolean ifExisting) {
        this.ifExisting = ifExisting;
    }

    public boolean hasRequiredNameAndValue() {
        return name != null && !name.isBlank() && value != null;
    }

    /**
     * Converts header to concatenated format "name=value"
     */
    public String toConcatenatedFormat() {
        if (name == null || value == null) {
            return "";
        }
        return name + "=" + value;
    }

    /**
     * Parse from concatenated format "name=value"
     */
    public static StaticHeader parseFromConcatenated(String concatenated) {
        if (concatenated == null || !concatenated.contains("=")) {
            return null;
        }
        int equalsIndex = concatenated.indexOf('=');
        String name = concatenated.substring(0, equalsIndex).trim();
        String value = concatenated.substring(equalsIndex + 1).trim();
        if (name.isBlank() || value.isBlank()) {
            return null;
        }
        StaticHeader header = new StaticHeader();
        header.setName(name);
        header.setValue(value);
        return header;
    }
}
