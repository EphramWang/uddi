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
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BindingDetail;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.ServiceInfo;
import org.uddi4j.response.ServiceList;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 * Sample code that exercises the publish API. Attempts
 * to save a businessService, finds the saved businessService and then
 * deletes the saved businessService.
 *
 * <OL>
 * <LI>Sets up an UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Saves a businessEntity.
 * <LI>Saves a businessService.
 * <LI>Finds a saved businessService.
 * <LI>Deletes the saved businessService.
 * <LI>Cleans up the data structures created (businessEntity)
 * </OL>
 *
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */
public class BusinessServiceExample
{

	Properties config = null;

	public static void main (String args[])
	{
		BusinessServiceExample app = new BusinessServiceExample();
		System.out.println("\n*********** Running BusinessServiceExample ***********");
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

			System.out.println("\nSave '" + config.getProperty("serviceName") + "'");

			// Create minimum required data objects

			Vector entities = new Vector();

			// For saving a Business Service we need a Business Entity .
			// Hence create a new business entity . Name is the business name.
			// BusinessKey must be "" and not null to save a new  business.
			BusinessEntity be = new BusinessEntity("", config.getProperty("businessName"));
			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

			// Process returned BusinessDetail object to get the
			// busines key.
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
			String businessKey = returnedBusinessEntity.getBusinessKey();

			// Create a new business service using BindingTemplates default
			// constructor.
			// DefaultName is the service name. ServiceKey must be "" to save a new service
			BusinessService businessService = new BusinessService("");
			businessService.setDefaultNameString(config.getProperty("serviceName"),null);
			businessService.setBusinessKey(businessKey);
			Vector services = new Vector();
			services.addElement(businessService);

			// **** First save a Business Service
			ServiceDetail serviceDetail = proxy.save_service(token.getAuthInfoString(),services);

			// Process returned ServiceDetail object to list the
			// saved services.
			Vector businessServices = serviceDetail.getBusinessServiceVector();
			BusinessService businessServiceReturned = (BusinessService)(businessServices.elementAt(0));
			String serviceKey = businessServiceReturned.getServiceKey();
			System.out.println("The saved Service : "+ businessServiceReturned.getDefaultNameString());
			System.out.println("The ServiceKey    : "+ serviceKey);

			System.out.println("\nFinding Service saved in first step");

			//creating vector of Name Object
			Vector names = new Vector();
			names.add(new Name(config.getProperty("serviceName")));

			// Setting FindQualifiers to 'exactNameMatch'
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("exactNameMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// **** Find the Business Service saved.
			// And setting the maximum rows to be returned as 5.
			ServiceList serviceList = proxy.find_service(businessKey, names, null,null,findQualifiers,5);

			// Process the returned ServiceList object
			Vector serviceInfoVector = serviceList.getServiceInfos().getServiceInfoVector();
			for( int i = 0; i < serviceInfoVector.size(); i++ )
			{
				ServiceInfo serviceInfo = (ServiceInfo)serviceInfoVector.elementAt(i);
				// Print name for each service
				System.out.println("Name of Service : " + serviceInfo.getDefaultNameString());
				System.out.println("Service key     : " + serviceInfo.getServiceKey());
				BindingDetail bindingDetail = proxy.find_binding(, erviceInfo.getServiceKey(), , );
			}

			System.out.println("\nDeleting the saved service");
			// Try to delete already saved Business Service. Delete will fail for services
			// not created by this id
			// Print name for each service
			System.out.println("Name of Service : " +
												 businessServiceReturned.getDefaultNameString());
			System.out.println("Service key     : " +
												 businessServiceReturned.getServiceKey());

			// **** Having service key, delete using the authToken
			DispositionReport dr = proxy.delete_service (token.getAuthInfoString(),businessServiceReturned.getServiceKey());

			if( dr.success() )
			{
				System.out.println("Service successfully deleted");
			}
			else
			{
				System.out.println(" Error during deletion of Service\n"+
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


			// Tries to clean up the registry, for the structures we just created
			//(by deleting the BusinessEntity)
			System.out.println("\nCleaning the Data Structures added/saved from registry");
			{
				// delete using the authToken and businessKey
				DispositionReport dr1 = proxy.delete_business(token.getAuthInfoString(),businessKey);

				if( dr1.success() )
				{
					System.out.println("Registry successfully cleaned");
				}
				else
				{
					System.out.println(" Error during deletion of Business\n"+
														 "\n operator:" + dr1.getOperator() +
														 "\n generic:"  + dr1.getGeneric() );

					Vector results = dr1.getResultVector();
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
