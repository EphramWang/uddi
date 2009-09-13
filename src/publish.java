import java.util.Properties;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.OverviewDoc;
import org.uddi4j.datatype.binding.AccessPoint;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.uddi4j.datatype.binding.InstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceInfo;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.datatype.service.BusinessServices;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.ServiceInfo;
import org.uddi4j.response.ServiceList;
import org.uddi4j.response.TModelDetail;
import org.uddi4j.util.DiscoveryURL;
import org.uddi4j.util.DiscoveryURLs;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.TModelBag;

/**
 *
 *	publish a service
 *
 * @author ephram (ephram1987@gmail.com)
 */
public class publish
{

	Properties config = null;

	public static void main (String args[])
	{
		publish app = new publish();
		System.out.println("\n*********** Running publish ***********");
		app.run();
		System.exit(0);
	}

	public void run()
	{
		// Load configuration
		config = Configurator.load();

		// Construct a UDDIProxy object
		UDDIProxy proxy = new UDDIProxy();

		try
		{
			// Select the desired UDDI server node
			proxy.setInquiryURL(config.getProperty("inquiryURL"));
			proxy.setPublishURL(config.getProperty("publishURL"));

			// Get an authorization token
			System.out.println("\nGet authtoken");

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(config.getProperty("userid"),config.getProperty("password"));

			System.out.println("Returned authToken:" + token.getAuthInfoString());

			System.out.println("\nSave '" + config.getProperty("serviceName") + "'");

			// For saving a Binding Template we need a Business Entity,
			// Business Service and TModel. Hence create a new business entity
			// , Business Service and TModel.
			
			// Create minimum required data objects
			Vector entities = new Vector();

			// For saving a Business Service we need a Business Entity .
			// Hence create a new business entity . Name is the business name.
			// BusinessKey must be "" and not null to save a new  business.
			BusinessEntity be = new BusinessEntity("", config.getProperty("businessName"));		
			
			// Create a new business service using BindingTemplates default
			// constructor.
			// DefaultName is the service name. ServiceKey must be "" to save a new service
			BusinessService businessService = new BusinessService("");
			businessService.setDefaultNameString(config.getProperty("serviceName"),null);
			businessService.setBusinessKey("");
			//store service Qos info
			businessService.setDefaultDescriptionString(config.getProperty("qosInfo"));
			
			Vector services = new Vector();
			services.addElement(businessService);
			BusinessServices businessServices = new BusinessServices();
			businessServices.setBusinessServiceVector(services);
			
			// Adding the BusinessServices to the BusinessEntity
			be.setBusinessServices(businessServices);
		
			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

			// Process returned BusinessDetail object to get the
			// busines key and Service Key..
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
			String businessKey = returnedBusinessEntity.getBusinessKey();	
			Vector businessServicesReturned = returnedBusinessEntity.getBusinessServices().getBusinessServiceVector();
			BusinessService businessServiceReturned = (BusinessService) (businessServicesReturned.elementAt(0));
			String serviceKey = businessServiceReturned.getServiceKey();
			
			// Saving TModel
			Vector tModels = new Vector();
			TModel tModel = new TModel("",config.getProperty("tmodelName"));
			OverviewDoc od = new OverviewDoc();
			od.setOverviewURL(config.getProperty("URL"));
			tModel.setOverviewDoc(od);
			tModels.add(tModel);
			TModelDetail tModelDetail = proxy.save_tModel(token.getAuthInfoString(), tModels);
			
			// Creating TModelInstanceDetails
			Vector tModelVector = tModelDetail.getTModelVector();
			String tModelKey = ((TModel) (tModelVector.elementAt(0)))
					.getTModelKey();
			Vector tModelInstanceInfoVector = new Vector();
			TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo(tModelKey);
			InstanceDetails detail = new InstanceDetails();
			detail.setOverviewDoc(od);
			tModelInstanceInfo.setInstanceDetails(detail);
			tModelInstanceInfoVector.add(tModelInstanceInfo);
			TModelInstanceDetails tModelInstanceDetails = new TModelInstanceDetails();
			tModelInstanceDetails.setTModelInstanceInfoVector(tModelInstanceInfoVector);

			Vector bindingTemplatesVector = new Vector();

			// Create a new binding templates using required elements
			// constructor
			// BindingKey must be "" to save a new binding
			BindingTemplate bindingTemplate = new BindingTemplate("",tModelInstanceDetails, new AccessPoint(config.getProperty("accessPoint"), "http"));
			bindingTemplate.setServiceKey(serviceKey);
			bindingTemplatesVector.addElement(bindingTemplate);

			// **** Save the Binding Template
			BindingDetail bindingDetail = proxy.save_binding(token.getAuthInfoString(), bindingTemplatesVector);

			// Process returned BindingDetail object
			Vector bindingTemplateVector = bindingDetail.getBindingTemplateVector();
			BindingTemplate bindingTemplateReturned = (BindingTemplate) (bindingTemplateVector.elementAt(0));
			////System.out.println("Returned BindingKey: "+ bindingTemplateReturned.getBindingKey());


			// Creating the TModel Bag
			TModelBag tModelBag = new TModelBag();
			Vector tModelKeyVector = new Vector();
			tModelKeyVector.add(tModelKey);
			tModelBag.setTModelKeyStrings(tModelKeyVector);

			// **** Find the Binding Template .
			// And setting the maximum rows to be returned as 5.
			BindingDetail bindingDetailReturned = proxy.find_binding(null,serviceKey, tModelBag, 5);

			// Process returned BindingDetail object
			Vector bindingTemplatesFound = bindingDetailReturned.getBindingTemplateVector();
			BindingTemplate bindingTemplateFound = (BindingTemplate) (bindingTemplatesFound.elementAt(0));

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
						System.out.println("\n errCode:"  + r.getErrInfo().getErrCode() + "\n errInfoText:" + r.getErrInfo().getText());
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
