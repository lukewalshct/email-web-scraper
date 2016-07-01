/* Program name: FindEmails.java
 * Author: Luke Walsh
 * 
 * FindEmails is a program that accepts a domain name as a
 * parameter and finds all emails on discoverable pages
 * of that domain name.
 * 
 * Link to solution on GitHub: 
 * https://github.com/lukewalshct/email-web-scraper * 
 */

public class FindEmails
{
	public static void main(String[] args)
	{
		if (args.length != 1) 
		{
			System.out.println("Usage: FindEmails domain_name");
			System.exit(0);
		}
		
		String url = args[0];
		EmailFinder ef = new EmailFinder();
		
		ef.find(url);
		ef.printResults();
	}	
}