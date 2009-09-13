/*
 * The source code contained herein is licensed under the IBM Public License
 * Version 1.0, which has been approved by the Open Source Initiative.
 * Copyright (C) 2001, Hewlett-Packard Company
 * All Rights Reserved.
 *
 */

import java.util.Properties;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.assertion.PublisherAssertion;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.RelatedBusinessInfo;
import org.uddi4j.response.RelatedBusinessesList;
import org.uddi4j.response.Result;
import org.uddi4j.util.KeyedReference;


/**
 * Sample code that exercises the publish API.The Publisher Assertion
 * is used to express the relationship exist between BusinessEntities.This
 * sample attempts to add a publisherAssertion , finds the related Businesses
 * and then delete the publisherAssertion .
 *
 * <OL>
 * <LI>Sets up a UDDIProxy object
 * <LI>Requests an authorization token
 * <LI>Save one business & find one business for asserting a relationship
 *     with each other.
 * <LI>Add a PublisherAssertion between two Business Entity
 * <LI>Lists businesses Asserted by BusinessKey holder.
 * <LI>Deletes the PublisherAssertion added.
 * <LI>Cleans up the Data Structures created.
 * </OL>
 *
 * @author Rajesh Sumra (rajesh_sumra@hp.com)
 */
public class PublisherAssertionExample
{

	Properties config = null;

	public static void main (String args[])
	{
		PublisherAssertionExample app = new PublisherAssertionExample();
		System.out.println("\n*********** Running PublisherAssertionExample ***********");
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
			System.out.println("\n Get authtoken");

			// Pass in userid and password registered at the UDDI site
			AuthToken token = proxy.get_authToken(config.getProperty("userid"),config.getProperty("password"));

			System.out.println(" Returned authToken:" + token.getAuthInfoString());

			// PublisherAssertion is created between two Business Entities.
			// Search for a business whose name is in the configuration file
			// and assert a relationship with the newly created business .
			// The relationship is picked up from the configuration file.

			System.out.println("\n Saving one Business Entity for PublisherAssertion");

			// Create minimum required data objects

			System.out.println(" Finding One Business Entity for PublisherAssertion");
			// Find one more Business Entity from registry, to be used for
			// asserting a relationship with former saved business entity.

			//creating vector of Name Object
			Vector names = new Vector();
			names.add(new Name(config.getProperty("sampleEntityName")));

			// Finds one more business .
			// And setting the maximum rows to be returned as 5.
			String toKey = "";
			BusinessList businessList = proxy.find_business(names, null, null, null,null,null,5);
			Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();
			if( businessInfoVector.size() > 0 )
			{
				BusinessInfo bi = (BusinessInfo)businessInfoVector.elementAt(0);
				toKey = bi.getBusinessKey();
			}
			else
			{
				System.out.println(" No businesses found....");
				System.out.println(" Please change the Business Entity name to be searched !!");
				System.exit(0);
			}

			Vector entities = new Vector();

			// Create one new business entity using required elements constructor
			// These will be used for PublisherAssertion.Name is the business name.
			// BusinessKey must be "" to save a new business
			BusinessEntity be = new BusinessEntity("", config.getProperty("businessName"));
			entities.addElement(be);

			// Save business
			BusinessDetail bd = proxy.save_business(token.getAuthInfoString(),entities);

			// Process returned BusinessDetail object
			Vector businessEntities = bd.getBusinessEntityVector();
			BusinessEntity returnedBusinessEntity = (BusinessEntity)(businessEntities.elementAt(0));


			// Get FromKey And ToKey from Business Entities returned and found
			String fromKey = returnedBusinessEntity.getBusinessKey();

			// Create KeyedReference with relationShip between
			// FromKey and Tokey . And set the TModelKey
			// so that it refers to uddi-org:relationships
			KeyedReference keyedReference = new KeyedReference("Holding Company",config.getProperty("assertionRelationship"));

			keyedReference.setTModelKey(TModel.RELATIONSHIPS_TMODEL_KEY);

			// Create PublisherAssertion using KeyedReference, Fromkey
			// and Tokey. The Publisher Assertion is used to express the
			// relationship exist between BusinessEntities.
			PublisherAssertion publisherAssertion =
			new PublisherAssertion(fromKey,toKey,keyedReference);

			System.out.println("\n Adding PublisherAssertions");
			System.out.println(" FromKey : " + fromKey);
			System.out.println(" ToKey   : " + toKey);

			//  **** Add the  PublisherAssertion
			DispositionReport dispositionReport=proxy.add_publisherAssertions(token.getAuthInfoString(),
																																			  publisherAssertion);


			System.out.println("\n Finding out the businesses which are related");

			// Find related businesses using business key and keyedReference
			// maxRows set at 0 means to return as many results as possible
			RelatedBusinessesList relatedBusinessesList =
			proxy.find_relatedBusinesses(fromKey, keyedReference, null, 0);

			System.out.println(" The businesses which are related to '"+ config.getProperty("businessName") + "': ");

			Vector relatedBusinessInfoVector  =
			relatedBusinessesList.getRelatedBusinessInfos().
			getRelatedBusinessInfoVector();
			for( int i = 0; i < relatedBusinessInfoVector.size(); i++ )
			{
				RelatedBusinessInfo relatedBusinessInfo =
				(RelatedBusinessInfo)relatedBusinessInfoVector.elementAt(i);

				// Print name for each businesses
				System.out.println(" " + relatedBusinessInfo.getDefaultNameString());
			}

			//  **** Deletes the PublisherAssertion
			System.out.println("\n Deleting the PublisherAssertion added in First step");
			dispositionReport = proxy.delete_publisherAssertions (token.getAuthInfoString(),publisherAssertion);
			if( dispositionReport.success() )
			{
				System.out.println(" PublisherAssertion successfully deleted");
			}
			else
			{
				System.out.println(" Error during deletion of PublisherAssertion\n"+
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
			System.out.println("\n Cleaning the Data Structures added/saved from registry");
			{
				// delete using the authToken and businessKey
				DispositionReport dr = proxy.delete_business(token.getAuthInfoString(),
																										 fromKey);

				if( dr.success() )
				{
					System.out.println(" Registry successfully cleaned");
				}
				else
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
