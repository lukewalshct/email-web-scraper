/* 
 * Class: EmailFinder
 * Author: Luke Walsh
 * 
 * EmailFinder is a class that represents a web crawler
 * object that searches a given domain and prints out any 
 * email found within discoverable pages within that
 * domain.
 *  
 * It operates by using a web driver to simulate a browser
 * and scraping each page it encounters in the url queue.
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.validator.routines.UrlValidator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class EmailFinder
{
	private LinkedList<String> sitesToVisit;
	private HashSet<String> sitesChecked;
	private HashSet<String> emails;
	private WebDriver webDriver;
	private String domainUrl;
	private int numVisited;
	
	//update the string below to the location of the installed ChromeDriver
	private static final String CHROME_DRIVER_PATH =
			"C:\\Users\\Luke\\Downloads\\chromedriver_win32\\chromedriver.exe";
	//optional - set the max # of page visits (-1 indicates no limit)
	private static final int MAX_VISITS = 50;
	//set regex's for url and emails
	private static final Pattern EMAIL_REGEX =
		Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
	private static final Pattern URL_REGEX =
		Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	
	/*
	 * Class constructor. 
	 */
	public EmailFinder()
	{
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
	}
	
	/*
	 * The find() method is the primary method that drives the web crawling
	 * for the email address. It accepts an url which represents the home
	 * page of a domain and loops through all the pages in a queue until
	 * it is empty (all discoverable pages visited) or MAX_VISITS is 
	 * reached. 
	 * 
	 * @param	url	the domain name
	 */
	public void find(String url)
	{
		//format url into full http address (ex. http://www.jana.com)
		String startUrl = formatStartUrl(url);

		//check to ensure URL is valid
		if(!isValidUrl(startUrl)) handleInvalidStartUrl(url);
		
		//get the domain name
		try
		{
			this.domainUrl = getDomainName(startUrl);
		}
		catch (URISyntaxException ex)
		{
			handleInvalidStartUrl(url);
		}
		
		//check to ensure domain is the home page, otherwise exit program
		if (!formatStartUrl(this.domainUrl).equals(startUrl)) handleInvalidStartUrl(url);
		
		//reset the stored emails, sites visited and sites to visit
		resetHistory();
		
		//add the start url to the queue and initialize web driver
		this.sitesToVisit.add(startUrl);
		this.webDriver = new ChromeDriver();
		
		//maintain a queue and crawl through the URLs while the queue is empty
		//and max page visits not reached
		while(!this.sitesToVisit.isEmpty() &&
				(this.numVisited <= MAX_VISITS ||
				 MAX_VISITS < 0))
		{
			String nextPageUrl = this.sitesToVisit.pollFirst();
			String pageContent = getPageContent(nextPageUrl);
			extractEmails(pageContent);
			extractDomainURLs(pageContent);
			this.numVisited++;
		}
		
		this.webDriver.close();		
	}
	
	/*
	 * Adds prefixes to the url passed in to form the full prefix
	 * for an http protocol. Note: the url may still be invalid
	 * since this method does not validate the url.
	 * 
	 * @param	url	the url to format
	 * @return		the formatted url
	 */
	private String formatStartUrl(String url)
	{
		if(url == null) return "";
		if(url.startsWith("http://www.") || url.startsWith("https://www.")) return url;
		if(url.startsWith("www.")) return "http://" + url;
		return "http://www." + url;
	}
	
	/*
	 * Checks to ensure that the specified url is valid.
	 * 
	 * @param	url	the url to validate
	 * @return		boolean indicating whether url is valid
	 */
	private boolean isValidUrl(String url)
	{
		if (url == null || url == "") return false;
		String[] protocols = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(protocols);
		return urlValidator.isValid(url);
	}
	
	/*
	 * Called when a domain passed in by the user is invalid, 
	 * due to an invalid url, unsupported protocol (only http and https),
	 * or the url is not the home page of the domain.
	 * <p>
	 * Prints an error message to the user and exits the program.
	 * 
	 * @param	url	the invalid url given by the user
	 */
	private void handleInvalidStartUrl(String url)
	{
		System.out.println(url + " is not a valid domain name. Please ensure the following:" +
				"\n\n-The domain name has a valid URL" +
				"\n-The domain is the home page (e.g. jana.com rather than jana.com/contact" +
				"\n-Protocol is supported (http or https if including protocol)" +
				"\n\nProgram exiting...");
		System.exit(0);
	}
	
	/*
	 * Returns the domain name of the given url (e.g. jana.com)
	 * 
	 * @param	url	the url from which to extract the domain
	 * @return		the domain name
	 * NOTE: this method was "borrowed" from StackOverflow:
	 * http://stackoverflow.com/questions/9607903/get-domain-name-from-given-url
	 */
	private String getDomainName(String url) throws URISyntaxException 
	{
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}		
	
	/*
	 * resetHistory() is called every time a new search is initiated
	 * from the find() method. It re-initializes the HashSets and 
	 * LinkedList that store the emails, sites checked, and sites
	 * visited to reset them for the new search domain.
	 */
	private void resetHistory()
	{
		this.numVisited = 0;
		this.sitesToVisit = new LinkedList<String>();
		this.sitesChecked = new HashSet<String>();
		this.emails = new HashSet<String>();
	}
	
	/*
	 * Extracts the page source from the given url, including dynamic 
	 * content loaded by JavaScript on initial page load.
	 * 
	 * @param	url	the url to get page source from
	 * @return		the page content
	 */
	private String getPageContent(String url)
	{		
		try
		{
			this.webDriver.get(url);		
			return webDriver.getPageSource();
		}
		catch (Exception ex)
		{
			return new String("");
		}
	}
	
	/*
	 * Uses regex pattern matching to extract emails from 
	 * the given page content. Matches standard email formats
	 * (close to 100%) but may not capture all valid emails.
	 * Adds the emails to the search's email HashSet if it's
	 * not already there.
	 * 
	 * @param content the page content to search for emails
	 */
	private void extractEmails(String content)
	{
		Matcher m = EMAIL_REGEX.matcher(content);
		while (m.find()) {
			String email = m.group().toString();
			if(!this.emails.contains(email))
			{
				this.emails.add(email);
			}
		}
	}
  
	/*
	 * Uses regex pattern matching to extract urls from the 
	 * given page content. Adds the url to the search's 
	 * list of sites to visit if it's not already in there or
	 * in the list of sites already visited. Excludes invalid
	 * urls and urls not in the domain specified by the user.
	 * 
	 * @param content the page content to search for urls
	 */
	private void extractDomainURLs(String content)
	{
		Matcher m = URL_REGEX.matcher(content);
		while (m.find()) {
			String url = m.group().toString();
			if(shouldVisit(url))
			{
				this.sitesToVisit.add(url);
			}
		}
	}
	
	/*
	 * Checks whether an url should be visited, based
	 * on whether the domain name matches the domain name
	 * supplied by the user, the url is not already visited or
	 * marked that it was already checked, and it is a valid url.
	 * 
	 * @param	url	the url to check
	 * @return		whether the page should be added to sites to visit
	 */
	private boolean shouldVisit(String url)
	{
		boolean shouldVisit = true;
		String domain = "";
		try
		{
			domain = getDomainName(url); 	
		}
		catch (Exception ex)
		{
			shouldVisit = false;
		}		
		if(this.sitesChecked.contains(url) || 
				this.sitesToVisit.contains(url) ||
				!(domain.contains(this.domainUrl)) ||
				!isValidUrl(url))
		{
			shouldVisit = false;
		}
		
		this.sitesChecked.add(url);

		return shouldVisit;
	}
	
	/*
	 * Prints the emails found by the program.
	 */
	public void printResults()
	{
		System.out.println("Found these emails:");
		for (String s: this.emails)
		{
			System.out.println(s);
		}
	}
}