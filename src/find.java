import java.util.Properties;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.binding.AccessPoint;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceInfo;
import org.uddi4j.response.ServiceList;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 
 * @author ephram (ephram1987@gmail.com)
 */

public class find
{
	Properties config = null;

	public static void main (String args[])
	{
		find app = new find();
		System.out.println("\n*********** Running find ***********");
		app.run();
		System.exit(0);
	}

	public void run()
	{
		// Load configuration
		config = Configurator.load();

		// Construct a UDDIProxy object.
		UDDIProxy proxy = new UDDIProxy();

		try
		{
			// Select the desired UDDI server node
			proxy.setInquiryURL(config.getProperty("inquiryURL"));
			proxy.setPublishURL(config.getProperty("publishURL"));

			//creating vector of Name Object
			Vector names = new Vector();
			names.add(new Name("book"));

			// Setting FindQualifiers to 'exactNameMatch' ||caseSensitiveMatch
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("caseSensitiveMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// Find service by name
			ServiceList serviceList = proxy.find_service(null, names, null,null,findQualifiers,0);
			// Process the returned ServiceList object
			Vector serviceInfoVector = serviceList.getServiceInfos().getServiceInfoVector();
			
			for( int i = 0; i < serviceInfoVector.size(); i++ )
			{
				ServiceInfo serviceInfo = (ServiceInfo)serviceInfoVector.elementAt(i);
				// Print name for each service
				System.out.println("Name of Service : " + serviceInfo.getDefaultNameString());
				System.out.println("Service key     : " + serviceInfo.getServiceKey());
				/**print accessPoint info*/
				BindingDetail bindingDetail = proxy.find_binding(null, serviceInfo.getServiceKey(), null, 0);
				Vector bindingTemplateVector = bindingDetail.getBindingTemplateVector();
				BindingTemplate bindingTemplate = (BindingTemplate)(bindingTemplateVector.elementAt(0));
				AccessPoint accessPoint = bindingTemplate.getAccessPoint();
				System.out.println("AccessPoint     : " + accessPoint.getText());
				/**print businessName info*/
				BusinessDetail bd = proxy.get_businessDetail(serviceInfo.getBusinessKey());
				Vector businessEntityVector = bd.getBusinessEntityVector();
				BusinessEntity be = (BusinessEntity) businessEntityVector.elementAt(0);
				System.out.println("BusinessName	: "+be.getDefaultNameString());
				
				/**print qos info*/
				for(int j=0;j<be.getBusinessServices().getBusinessServiceVector().size();j++)
				{
					BusinessService businessService = (BusinessService) be.getBusinessServices().getBusinessServiceVector().elementAt(j);
					if(businessService.getDefaultNameString().compareTo(serviceInfo.getDefaultNameString())==0)
						System.out.println("Qos Info	: "+businessService.getDefaultDescriptionString());
				}
			}				
		}
		catch( UDDIException e )
		{
			DispositionReport dr = e.getDispositionReport();
			if( dr!=null )
			{
				System.out.println("UDDIException faultCode:" + e.getFaultCode() +
													 "\n operator:" + dr.getOperator() +
													 "\n generic:"  + dr.getGeneric() );

				Vector results = dr.getResultVector();
				for( int i=0; i<results.size(); i++ )
				{
					Result r = (Result)results.elementAt(i);
					System.out.println("\n errno:"    + r.getErrno() );
					if( r.getErrInfo()!=null )
					{
						System.out.println("\n errCode:"  + r.getErrInfo().getErrCode() +
															 "\n errInfoText:" + r.getErrInfo().getText());
					}
				}
			}
			e.printStackTrace();			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
