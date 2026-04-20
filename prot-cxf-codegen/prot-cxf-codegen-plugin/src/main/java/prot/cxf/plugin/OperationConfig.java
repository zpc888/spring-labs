package prot.cxf.plugin;

public class OperationConfig {

	private String action;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String resolveAction(String fallbackOperationName) {
		if (action != null && !action.isBlank()) {
			return action;
		}
		return fallbackOperationName;
	}
}

