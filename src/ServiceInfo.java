
import java.io.Serializable;

/**
 * The service info for search verify and evaluate.
 * @author Administrator
 *
 */
public class ServiceInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3736330799489627645L;

	/** Service provider name. */
	private String businessName;
	
	/** The service name. */
	private String serviceName;
	
	/** The service url. */
	private String accessPoint;
	
	/** The service wsdl url. */
	private String overviewURL;
	
	/** The service qos url. */
	private String qosInfo;

	public ServiceInfo() {
		businessName = new String();
		serviceName = new String();
		accessPoint = new String();
		overviewURL = new String();
		qosInfo = new String();
	}

	String getBusinessName() {
		return businessName;
	}

	void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	String getServiceName() {
		return serviceName;
	}

	void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	String getAccessPoint() {
		return accessPoint;
	}

	void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}

	String getOverviewURL() {
		return overviewURL;
	}

	void setOverviewURL(String overviewURL) {
		this.overviewURL = overviewURL;
	}

	String getQosInfo() {
		return qosInfo;
	}

	void setQosInfo(String qosInfo) {
		this.qosInfo = qosInfo;
	}
}
