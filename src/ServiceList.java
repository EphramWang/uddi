

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The list to store the same kind of service. Also the parameter type of the
 * trust platform.
 * 
 * @author Administrator
 * 
 */
public class ServiceList implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1018056977393680550L;
	
	private ArrayList<ServiceInfo> serviceList;

	public ServiceList() {
		serviceList = new ArrayList<ServiceInfo>();
	}
	
	public boolean addServiceInfo(ServiceInfo serviceInfo) {
		return serviceList.add(serviceInfo);
	}
	
	public boolean removeServiceInfo(ServiceInfo serviceInfo) {
		return serviceList.remove(serviceInfo);
	}

	public ArrayList<ServiceInfo> getServiceList() {
		return serviceList;
	}

	public void setServiceList(ArrayList<ServiceInfo> serviceList) {
		this.serviceList = serviceList;
	}
}
