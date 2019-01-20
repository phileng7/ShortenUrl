package api.shortenurl;

import java.sql.Connection;

public interface ShortenURL {

	/** connection with the database
	 * 
	 * @param propertiesFilePath
	 * 		directory where the db.properties is found
	 * @param prefixName 
	 * 		prefix to search inside db.properties, ex: "mysql" for "mysql".driver
	 * @param message
	 * 		return message error
	 * @return - the database connection, if null not OK, read message
	 */
	public Connection connection(String propertiesFilePath, String prefixName, StringBuilder message);
	
	
	/** input long Url returns short Url
	 * 
	 * @param domain
	 * 		Domain to be used, ex: http://sht.ly
	 * @param longUrl
	 * 		Long original URL to be shortened
	 * @param con
	 * 		inform the database connection	 
	 * @param message
	 * 		return message error
	 * @return - short URL, if null not OK, read message
	 */
	public String shortenUrl(String domain, String longUrl, Connection con, StringBuilder message);
	
	
	/** input short Url, convert to long Url and increments statistics
	 * 
	 * @param shortUrl, ex: http://sht.ly/HCR
	 * 		receive a short URL (search database)
	 * @param longUrl
	 * 		Returns Long URL with the original URL to navigate	 
	 * @param con
	 * 		inform the database connection
	 * @param message
	 * 		return message error 
	 * @return - Returns Long URL with the original URL to navigate, if null not OK, read message
	 */
	public String shortNavigation(String shortUrl, Connection con, StringBuilder message);
	
	/** input short Url, return number of access
	 * 
	 * @param shortUrl, ex: http://sht.ly/HCR
	 * 		receive a short URL (search database)
	 * @param con
	 * 		inform the database connection
	 * @param message
	 * 		return message error 
	 * @return - returns number of access, if -1 not OK, read message
	 */
	public Integer getStat(String shortUrl, Connection con, StringBuilder message);
}
