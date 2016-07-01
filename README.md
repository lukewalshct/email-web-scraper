# email-web-scraper
Java command line program that extracts emails found on discoverable pages for a given domain name.

#Synopsis

This program is a web crawler written in Java that takes a domain name from the user via command 
line input, and searches through discoverable pages in that domain name and prints the resulting emails.

# Setup

This project utilizes two additional libraries:
-Selenium (for the web driver)
-Apache Commons URLValidator

The following is a set of instructions to run this program on your compunter:

1. Clone this repo onto your local machine
2. Donwload Selenium for Java, here: http://www.seleniumhq.org/download/
3. Add Selenium .jar to your class path variable
4. Download ChromeDriver, the specific web driver that runs the web crawler, here:
   http://chromedriver.storage.googleapis.com/index.html?path=2.22/
5. Save ChromeDriver to a local folder, and update the location of ChromeDriver
   for the CHROME_DRIVER_PATH variable in line 34 of EmailFinder.java
6. Download the commons-validator-1.4.1-bin.zip from Apache Commons, here:
	http://commons.apache.org/proper/commons-validator/download_validator.cgi
7. Extract commons-validator-1.4.1-bin.zip and add to your class path
8. [Optional] Set the MAX_VISITS variable in the EmailFinder class if you would 
   like the program to stop after a certain number of page visits (default is 50)
9. Compile FindEmails.java


# Using the Program

Once the above setup steps are complete, the program should be ready to run.
The usage is as follows:

FindEmails domain_name

The domain name must satisfy these requirements:
-Is a valid domain name
-Is the home page of the domain (e.x. mit.edu is acceptable, but mit.edu/aboutmit is not)
-Is a supported protocol (http or https)


#Improvements/Next Steps

Some potential ideas for improving the project:

1. Investiage using a headless browser rather than ChromeDriver, as rendering slows down
  the crawler.
2. Explore using multi-threading to speed up the crawling.
3. Explore other libraries that may better handle dynamic content w/JavaScript.
4. If more exact email matching is needed, look into finding a more complete regular expression.
5. Thorough unit testing.