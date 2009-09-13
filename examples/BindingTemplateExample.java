/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, International Business Machines Corporation
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

import java.util.Properties;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.binding.AccessPoint;
import org.uddi4j.datatype.binding.BindingTemplate;
import org.uddi4j.datatype.binding.TModelInstanceDetails;
import org.uddi4j.datatype.binding.TModelInstanceInfo;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.datatype.service.BusinessServices;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.TModelDetail;
import org.uddi4j.util.TModelBag;

/**
 * Sample code that exercises the publish API. Attempts
 * to save a bindingTemplate, finds the bindingTemplate and then
 * deletes the bindingTemplate.
 *
 * <OL>
 * <LI>Sets up an UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Saves a businessEntity containing a businessService.
 * <LI>Saves a tModel.
 * <LI>Saves bindingTemplate refering to the tModel just created
 * <LI>Finds bindingTemplate using the tModelBag.
 * <LI>Deletes bindingTemplate using the bindingKey.
 * <LI>Cleans up all the data structures created.
 * </OL>
 *
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */
public class BindingTemplateExample
{

	Properties config = null;

	public static void main (String args[])
	{
		BindingTemplateExample app = new BindingTemplateExample();
		System.out.println("\n*********** Running BindingTemplateExample ***********");
		app.run();
		System.exit(0);
	}

	public void run()
	{
		// Load samples configuration
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

			System.out.println("\nSaving a BindingTemplate");

			// Create minimum required data objects
			Vector entities = new Vector();

			// For saving a Binding Template we need a Business Entity,
			// Business Service and TModel. Hence create a new business entity
			// , Business Service and TModel.

			// Name is the business name. BusinessKey must be "" to save a new
			// business
			BusinessEntity be =
			new BusinessEntity("", config.getProperty("businessName"));

			// Create a new business service using BindingTemplates
			// DefaultName is the service name. ServiceKey must be "" to save a new service
			BusinessService businessService = new BusinessService("");
			businessService.setDefaultNameString(config.getProperty("serviceName"),null);
			businessService.setBusinessKey ("");


			Vector services = new Vector();
			services.addElement(businessService);
			BusinessServices businessServices = new BusinessServices ();
			businessServices.setBusinessServiceVector (services);

			// Adding the BusinessServices to the BusinessEntity
			be.setBusinessServices (businessServices);

			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

			// Process returned BusinessDetail object to get the
			// Business Key and Service Key.
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity =
			(BusinessEntity)(businessEntities.elementAt(0));
			String businessKey =
			returnedBusinessEntity.getBusinessKey();
			Vector businessServicesReturned =
			returnedBusinessEntity.getBusinessServices().getBusinessServiceVector();
			BusinessService businessServiceReturned =
			(BusinessService)(businessServicesReturned.elementAt(0));
			String serviceKey = businessServiceReturned.getServiceKey();

			// Saving TModel
			Vector tModels = new Vector();
			TModel tModel = new TModel("", config.getProperty("tmodelName"));
			tModels.add(tModel);
			TModelDetail tModelDetail = proxy.save_tModel(token.getAuthInfoString(), tModels);

			// Creating TModelInstanceDetails
			Vector tModelVector = tModelDetail.getTModelVector();
			String tModelKey = ((TModel)(tModelVector.elementAt(0))).getTModelKey();
			Vector tModelInstanceInfoVector = new Vector();
			TModelInstanceInfo tModelInstanceInfo = new TModelInstanceInfo(tModelKey);
			tModelInstanceInfoVector.add(tModelInstanceInfo);
			TModelInstanceDetails tModelInstanceDetails = new TModelInstanceDetails();
			tModelInstanceDetails.setTModelInstanceInfoVector(tModelInstanceInfoVector);

			Vector bindingTemplatesVector = new Vector();

			// Create a new binding templates using required elements constructor
			// BindingKey must be "" to save a new binding
			BindingTemplate bindingTemplate = new BindingTemplate("",
																														tModelInstanceDetails,
																														new AccessPoint("www.uddi.org","http"));
			bindingTemplate.setServiceKey(serviceKey);
			bindingTemplatesVector.addElement(bindingTemplate);

			// **** Save the Binding Template
			BindingDetail bindingDetail = proxy.save_binding(token.getAuthInfoString(),
																											 bindingTemplatesVector);

			// Process returned BindingDetail object
			Vector bindingTemplateVector = bindingDetail.getBindingTemplateVector();
			BindingTemplate bindingTemplateReturned = (BindingTemplate)(bindingTemplateVector.elementAt(0));
			System.out.println("Returned BindingKey: " +
												 bindingTemplateReturned.getBindingKey());

			System.out.println("\nFinding the BindingTemplate saved ");

			// Creating the TModel Bag
			TModelBag tModelBag = new TModelBag();
			Vector tModelKeyVector = new Vector();
			tModelKeyVector.add(tModelKey);
			tModelBag.setTModelKeyStrings(tModelKeyVector);

			// **** Find the Binding Template .
			// And setting the maximum rows to be returned as 5.
			BindingDetail bindingDetailReturned = proxy.find_binding(null, serviceKey, tModelBag, 5);

			// Process returned BindingDetail object
			Vector bindingTemplatesFound = bindingDetailReturned.getBindingTemplateVector();
			BindingTemplate bindingTemplateFound = (BindingTemplate)(bindingTemplatesFound.elementAt(0));
			System.out.println("BindingKey Found: " + bindingTemplateFound.getBindingKey());


			System.out.println("\nDeleting the saved BindingTemplate");

			// **** Delete Binding Template using the Binding Key returned
			DispositionReport dispositionReport =
			proxy.delete_binding(token.getAuthInfoString(),
													 bindingTemplateReturned.getBindingKey());
			if( dispositionReport.success() )
			{
				System.out.println("BindingKey : " + bindingTemplateReturned.getBindingKey());
				System.out.println("Binding Template successfully deleted");
			}
			else
			{
				System.out.println(" Error during deletion of BindingTemplate\n"+
													 "\n operator:" + dispositionReport.getOperator() +
													 "\n generic:"  + dispositionReport.getGeneric() );

				Vector results = dispositionReport.getResultVector();
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


			// Tries to clean up the registry, for the structures we just created
			//(by deleting the BusinessEntity)
			System.out.println("\nCleaning the Data Structures added/saved from registry");
			{
				// delete using the authToken and businessKey
				DispositionReport dr = proxy.delete_business(token.getAuthInfoString(),
																										 businessKey);

				if( !dr.success() )
				{
					System.out.println(" Error during deletion of Business\n"+
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

				// delete TModel using authToken and tModelKey
				dr = proxy.delete_tModel(token.getAuthInfoString(),
																 tModelKey);
				if( dr.success() )
				{
					System.out.println("Registry successfully cleaned");
				}
				else
				{
					System.out.println(" Error during deletion of tModel\n"+
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
			}
		}
		// Handle possible errors
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
		// Catch any other exception that may occur
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
