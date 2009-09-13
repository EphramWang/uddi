import java.util.Properties;
import java.util.Vector;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.Name;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;

/**
 *  delete a business's services
 *
 * @author ephram (ephram1987@gmail.com)
 */
public class delete
{

	Properties config = null;

	public static void main (String args[])
	{
		delete app = new delete();
		System.out.println("\n*********** Running delete ***********");
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

			System.out.println("Search for '" + config.getProperty("businessName") + "' to delete");

			//creating vector of Name Object
			Vector names = new Vector();
			names.add(new Name(config.getProperty("businessName")));

			// Setting FindQualifiers to 'caseSensitiveMatch'
			FindQualifiers findQualifiers = new FindQualifiers();
			Vector qualifier = new Vector();
			qualifier.add(new FindQualifier("caseSensitiveMatch"));
			findQualifiers.setFindQualifierVector(qualifier);

			// Find businesses by Business name
			// And setting the maximum rows to be returned as 5.
			BusinessList businessList = proxy.find_business(names, null, null, null,null,findQualifiers,5);

			Vector businessInfoVector  = businessList.getBusinessInfos().getBusinessInfoVector();

			// Try to delete any businesses with this name. Multiple businesses with the same
			// name may have been created by different userids. Delete will fail for businesses
			// not created by this id
			for( int i = 0; i < businessInfoVector.size(); i++ )
			{
				BusinessInfo bi = (BusinessInfo)businessInfoVector.elementAt(i);
				System.out.println("Found business key:" + bi.getBusinessKey());

				// Have found the matching business key, delete using the authToken
				DispositionReport dr = proxy.delete_business(token.getAuthInfoString(),bi.getBusinessKey());

				if( dr.success() )
				{
					System.out.println("Business successfully deleted");
				}
				else
				{
					System.out.println(" Error during deletion of Business\n"+
														 "\n operator:" + dr.getOperator() +
														 "\n generic:"  + dr.getGeneric() );

					Vector results = dr.getResultVector();
					for( int j=0; j<results.size(); j++ )
					{
						Result r = (Result)results.elementAt(j);
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

