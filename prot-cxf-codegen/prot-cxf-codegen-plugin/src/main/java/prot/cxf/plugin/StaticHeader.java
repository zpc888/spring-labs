package prot.cxf.plugin;

public class StaticHeader {

    private String name;
    private String value;
    private Boolean ifExisting;

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
}

