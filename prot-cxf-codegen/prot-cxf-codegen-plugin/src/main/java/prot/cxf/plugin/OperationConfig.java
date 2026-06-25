package prot.cxf.plugin;

import java.util.List;

public class OperationConfig {

	private String action;
	private List<StaticHeader> staticHeaders;
	private List<String> dynamicHeaders;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String resolveAction(String wsdlSoapAction, String fallbackOperationName) {
		if (action != null && !action.isBlank()) {
			return action;
		}
		if (wsdlSoapAction != null && !wsdlSoapAction.isBlank()) {
			return wsdlSoapAction;
		}
		return fallbackOperationName;
	}

    public List<StaticHeader> getStaticHeaders() {
        return staticHeaders == null ? List.of() : staticHeaders;
    }

    public void setStaticHeaders(List<StaticHeader> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

    public List<String> getDynamicHeaders() {
        return dynamicHeaders == null ? List.of() : dynamicHeaders;
    }

    public void setDynamicHeaders(List<String> dynamicHeaders) {
        this.dynamicHeaders = dynamicHeaders;
    }
}

