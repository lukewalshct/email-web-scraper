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

/*
Improvements:
1. Look into getting headless browser library to make it run faster (rendering is painfully slow)
2. Look into different library that better handles dynamic javascript injections (some emails may be missed
 using current approach)
3. Look into multi-threading to speed up performance
4. More exact email regex matching
5. unit testing

*/