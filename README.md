# ShortenUrl
## Mandatory Requirements
- Design and implement an API for short URL creation
- Implement forwarding of short URLs to the original ones
- There should be some form of persistent storage
- The application should be distributed as one or more Docker images
## Additional Requirements
- Design and implement an API for gathering different statistics

It was created in Java.

The db.properties database configuration file is configured to work with MySQL database, but you can use other database.
The prefix name determines what database is configured. In this file we have mysql., but you can add oracle. for instance. The code that will use the database must have besides this ShortenUrl JAR file also the database connector JAR file.
In this configuration file it was created a database named shortenurldb with the user/password found inside this file. You can change at your wish.

Two tables are needed:<br>
CREATE TABLE `domain` (<br>
  `domain` varchar(100) NOT NULL,<br>
  `domain_id` int(11) NOT NULL DEFAULT '1',<br>
  PRIMARY KEY (`domain`)<br>
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table will associatethe domain to an ID';<br>
<br>
CREATE TABLE `shortenurl` (<br>
  `domain_id` int(11) NOT NULL,<br>
  `shorturl` int(11) NOT NULL DEFAULT '1000',<br>
  `longurl` mediumtext NOT NULL,<br>
  `statistics` int(11) DEFAULT NULL,<br>
  KEY `domain_id_fk_idx` (`domain_id`)<br>
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table with a domain ID and a Shorten URL associates to a Long URL';<br>
<br>
Take a look at ShortenURL interface.
Below a code that can be used to test this API:
 `
import java.sql.Connection;
import java.sql.SQLException;

import api.shortenurl.*;

public class App {
	Connection con;

	public static void main(String[] args) {
		App app = new App();
		app.createShortenUrls();
		app.getLongUrl();
		app.getNumberAccess();
	}

	// This testing method will invoke from the API the method connection
	// to establish a connection with a database
	public void createShortenUrls() {
		StringBuilder msgRes = new StringBuilder();
		ShortenURL shortenUrl = new ShortenURLImpl();
		
		/*	db.properties database configuration file is found in /temp path
			prefix name = mysql
			msgRes = message result error
			return the Connection */
		con = shortenUrl.connection("/temp", "mysql", msgRes);
		
		//if connection not available then returns null
		if (con == null) {
			System.out.println(msgRes);
			return;
		}
		msgRes.setLength(0);
		
		String shortUrl=null;
		
		//some URLs to be shortened		
		/*
		 * domain
		 * full URL
		 * Database Connection
		 * shortUrl = URL shortened
		 * res= Return message error (null=no error) 
		 */
		shortUrl = shortenUrl.shortenUrl("http://sht.ly",
				"https://www.google.com/search?hl=en&sugexp=les;&gs_nf=1&gs_mss=how%20do%20I%20iron%20a%20s&tok=POkeFnEdGVTAw_InGMW-Og&cp=21&gs_id=2j&xhr=t&q=how%20do%20I%20iron%20a%20shirt&pf=p&sclient=psy-ab&oq=how+do+I+iron+a+shirt&gs_l=&pbx=1&bav=on.2,or.r_gc.r_pw.r_cp.r_qf.&biw=1600&bih=775&cad=h\r\n",
				con, msgRes);		
		if (shortUrl == null) {
			System.out.println(msgRes);
			return;
		}
		System.out.println(shortUrl);
		msgRes.setLength(0);
		
		//other
		shortUrl = shortenUrl.shortenUrl("http://sht.ly",
				"https://travel.usnews.com/Hotels/review-Rosewood_London-London-England-132234/",
				con, msgRes);
		if (shortUrl == null) {
			System.out.println(msgRes);
			return;
		}
		System.out.println(shortUrl);
		msgRes.setLength(0);
		
		//other
		shortUrl = shortenUrl.shortenUrl("http://sht.ly",
				"https://globoesporte.globo.com/futebol/futebol-internacional/jogo/20-01-2019/huddersfield-manchester-city.ghtml",
				con, msgRes);
		if (shortUrl == null) {
			System.out.println(msgRes);
			return;
		}
		System.out.println(shortUrl);
		msgRes.setLength(0);
		
		// Close the database connection at the end
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getLongUrl() {
		StringBuilder msgRes = new StringBuilder();
		ShortenURL shortenUrl = new ShortenURLImpl();
		
		/*	db.properties database configuration file is found in /temp path
			prefix name = mysql
			msgRes = message result error
			return the Connection */
		con = shortenUrl.connection("/temp", "mysql", msgRes);
		
		//if connection not available then returns null
		if (con == null) {
			System.out.println(msgRes);
			return;
		}
		msgRes.setLength(0);
		
		String longUrl = shortenUrl.shortNavigation("http://sht.ly/HCR", con, msgRes);
		if (longUrl == null) {
			System.out.println(msgRes);
			return;
		}
		System.out.println(longUrl);
		msgRes.setLength(0);
		
		//force second access for statistics increment
		//longUrl = shortenUrl.shortNavigation("http://sht.ly/HCR", con, msgRes);
		
		// Close the database connection at the end
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getNumberAccess() {
		StringBuilder msgRes = new StringBuilder();
		ShortenURL shortenUrl = new ShortenURLImpl();
		
		/*	db.properties database configuration file is found in /temp path
			prefix name = mysql
			msgRes = message result error
			return the Connection */
		con = shortenUrl.connection("/temp", "mysql", msgRes);
		
		//if connection not available then returns null
		if (con == null) {
			System.out.println(msgRes);
			return;
		}
		msgRes.setLength(0);
		
		String shortUrl = "http://sht.ly/HCR";
		Integer numAccess = shortenUrl.getStat(shortUrl, con, msgRes);
		if (numAccess <0 ) {
			System.out.println(msgRes);
			return;
		}
		System.out.println("Numero de acessos a "+ shortUrl + " = " + numAccess);
		msgRes.setLength(0);
		
		// Close the database connection at the end
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
