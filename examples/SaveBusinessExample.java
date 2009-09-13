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
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 * Sample code that exercises the publish API. Attempts
 * to save a businessEntity.
 *
 * <OL>
 * <LI>Sets up a UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Saves a businessEntity
 * <LI>Lists businesses starting with the the first letter of the
 *     business's name. The new business should be in the list.
 * </OL>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */
public class SaveBusinessExample
{

	Properties config = null;

	public static void main (String args[])
	{
		SaveBusinessExample app = new SaveBusinessExample();
		System.out.println("\n*********** Running SaveBusinessExample ***********");
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

			System.out.println("\nSave '" + config.getProperty("businessName") + "'");

			// Create minimum required data objects
			Vector entities = new Vector();

			// Create a new business entity using required elements constructor
			// Name is the business name. BusinessKey must be "" to save a new
			// business
			BusinessEntity be = new BusinessEntity("", config.getProperty("businessName"));
			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

			// Process returned BusinessDetail object
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));
			System.out.println("Returned businessKey:" + returnedBusinessEntity.getBusinessKey());

			// Find all businesses that start with that particular letter e.g. 'S' for 'Sample Business'.
			String businessNameLeadingSubstring = (config.getProperty("businessName")).substring (0,1);
			System.out.println("\nListing businesses starting with " + businessNameLeadingSubstring
												 + " after we publish");

			//creating vector of Name Object
			Vector names = new Vector();
			names.add(new Name(businessNameLeadingSubstring));

			// Setting FindQualifiers to 'caseSensitiveMatch'
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("caseSensitiveMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// Find businesses by name
			// And setting the maximum rows to be returned as 5.
			BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);

			Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
			for( int i = 0; i < businessInfoVector.size(); i++ )
			{
				BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);
				System.out.println(businessInfo.getDefaultNameString());
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
