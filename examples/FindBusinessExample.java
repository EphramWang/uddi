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
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 * Sample code that exercises the Enquiry API. Attempts
 * to find a business by name.
 *
 * <OL>
 * <LI>Sets up a UDDIProxy object
 * <LI>Finds businesses by name.
 * </OL>
 *
 * @author David Melgar (dmelgar@us.ibm.com)
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */

public class FindBusinessExample
{
	Properties config = null;

	public static void main (String args[])
	{
		FindBusinessExample app = new FindBusinessExample();
		System.out.println("\n*********** Running FindBusinessExample ***********");
		app.run();
		System.exit(0);
	}

	public void run()
	{
		// Load samples configuration
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
			names.add(new Name("s"));

			// Setting FindQualifiers to 'caseSensitiveMatch'
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("caseSensitiveMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// Find businesses by name
			// And setting the maximum rows to be returned as 5.
			BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,0);

			Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
			for( int i = 0; i < businessInfoVector.size(); i++ )
			{
				BusinessInfo businessInfo = (BusinessInfo)businessInfoVector.elementAt(i);

				// Print name for each business
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
