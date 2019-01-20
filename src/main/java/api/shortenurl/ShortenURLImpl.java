package api.shortenurl;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ShortenURLImpl implements ShortenURL {
	public static final String CHARS = "0123456789bcdfghjkmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ-_";
	public static final int SIZE = CHARS.length();

	Properties props;

	@Override
	public Connection connection(String propertiesFilePath, String prefixName, StringBuilder message) {
		if (propertiesFilePath == null || propertiesFilePath.length() == 0) {
			// System.out.println("db.properties file path argument missing");
			message.append("db.properties file path argument missing");
			return null;
		}
		if (prefixName == null || prefixName.length() == 0) {
			// System.out.println("prefix found in db.properties file argument missing");
			message.append("prefix found in db.properties file argument missing");
			return null;
		}
		String fileName = propertiesFilePath + "/db.properties";

		props = new Properties();
		try (FileInputStream fis = new FileInputStream(fileName)) {
			props.load(fis);
		} catch (IOException ex) {
			// System.out.println("db.properties file not found");
			System.out.println(ex.getMessage());
			message.append("db.properties file not found");
			return null;
		}
		/*
		 * Not needed try { String driver = props.getProperty(prefixName + ".driver");
		 * // Returns the Class object associated with the class Class.forName(driver);
		 * } catch (ClassNotFoundException ex) { System.out.println(ex.toString());
		 * return "Driver Class not found"; }
		 */
		Connection con;
		try {
			String url = props.getProperty(prefixName + ".url");
			String username = props.getProperty(prefixName + ".username");
			String password = props.getProperty(prefixName + ".password");
			con = DriverManager.getConnection(url, username, password);
		} catch (SQLException ex) {
			ex.printStackTrace();
			message.append("Connection error");
			return null;
		}

		return con;
	}

	/**
	 * Get Shorten URL string and insert it with long URL into Database domain ex:
	 * http://sht.ly
	 */
	@Override
	public String shortenUrl(String domain, String longUrl, Connection con, StringBuilder message) {
		if (domain == null) {
			message.append("Inform the domain");
			return null;
		} else if (longUrl == null || longUrl.length() == 0) {
			message.append("Long URL is missing");
			return null;
		} else if (con == null) {
			message.append("No connection available. Execute connection first.");
			return null;
		}

		Integer shortUrlValue = Integer.parseInt(props.getProperty("init.number"));
		if (shortUrlValue < 100) {
			message.append("init.number invalid");
			return null;
		}

		String execSQL = "SELECT domain_id FROM domain WHERE domain=?";
		PreparedStatement ps;
		Integer domainId = 0;
		try {
			ps = con.prepareStatement(execSQL);
			ps.setString(1, domain);
			ResultSet rs = ps.executeQuery();
			// If exists use the domain ID, otherwise create a new one
			if (rs.next()) {
				domainId = rs.getInt(1);
			} else { // insert new domain
				// Get Max domain_id value and increments
				execSQL = "SELECT MAX(domain_id) FROM domain";
				rs = ps.executeQuery(execSQL);
				if (rs.next())
					domainId = rs.getInt(1);
				domainId++;

				execSQL = "INSERT INTO domain" + "(domain, domain_id) VALUES" + "(?,?)";
				ps = con.prepareStatement(execSQL);
				ps.setString(1, domain);
				ps.setInt(2, domainId);
				// execute insert SQL stetement
				ps.executeUpdate();
			}
			// Capture maximum value for short URL
			execSQL = "SELECT MAX(shorturl) FROM shortenurl WHERE domain_id=?";
			ps = con.prepareStatement(execSQL);
			ps.setInt(1, domainId);
			rs = ps.executeQuery();
			if (rs.next() && rs.getInt(1) != 0) {
				shortUrlValue = rs.getInt(1);
				shortUrlValue++;
			}
			// Insert into Short URL table
			execSQL = "INSERT INTO shortenurl" + "(domain_id,shorturl,longurl,statistics) VALUES" + "(?,?,?,0)";
			ps = con.prepareStatement(execSQL);
			ps.setInt(1, domainId);
			ps.setInt(2, shortUrlValue);
			ps.setString(3, longUrl);
			ps.executeUpdate();
			ps.close();
			execSQL = null;

			// Generate the short Url
			String conv = domain + "/" + numberToString(shortUrlValue);
			return conv;
		} catch (SQLException ex) {
			System.out.println(execSQL);
			ex.printStackTrace();
			message.append("Database query error.");
		}
		return null;
	}

	// Retrieve long URL from a Short one.
	@Override
	public String shortNavigation(String shortUrl, Connection con, StringBuilder message) {
		if (shortUrl == null) {
			message.append("Inform the short URL");
			return null;
		} else if (con == null) {
			message.append("No connection available. Execute connection first.");
			return null;
		}
		
		//Split domain from rest URL
		int pos = shortUrl.lastIndexOf('/');
		String domain = shortUrl.substring(0, pos);
		String rest = shortUrl.substring(pos+1, shortUrl.length());
		Integer restNum = stringToNumber(rest);
		Integer domainId=0;
		String longUrl=null;

		String execSQL = "SELECT dom.domain_id, sht.longurl, sht.statistics FROM domain dom, shortenurl sht WHERE dom.domain=? AND sht.shorturl=? AND dom.domain_id=sht.domain_id";
		PreparedStatement ps;
		Integer statistics = 0;

		try {
			ps = con.prepareStatement(execSQL);
			ps.setString(1, domain);
			ps.setInt(2, restNum);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				domainId = rs.getInt(1);
				longUrl = rs.getString(2);
				statistics = rs.getInt(3);
			} else {
				message.append("Short URL not found. Insert the new one.");
				return null;
			}
			statistics++;
			
			//update statistc plus one
			execSQL = "UPDATE shortenurl SET statistics=? WHERE domain_id=? AND shorturl=?";
			ps = con.prepareStatement(execSQL);
			ps.setInt(1, statistics);
			ps.setInt(2, domainId);
			ps.setInt(3, restNum);
			ps.executeUpdate();
			ps.close();
			execSQL = null;
		} catch (SQLException ex) {
			System.out.println(execSQL);
			ex.printStackTrace();
			message.append("Database query error.");
		}
		return longUrl;
	}

	/*
	 * While v is greater than 0: Take v and divide by SIZE. Store the remainder and
	 * let v = quotient Each v%SIZE has a position inside CHARS that represents
	 * Alphanumeric
	 */
	private String numberToString(Integer value) {
		StringBuilder str = new StringBuilder();
		while (value > 0) {
			str.insert(0, CHARS.charAt(value % SIZE));
			value = value / SIZE;
		}
		return str.toString();
	}
	
	/*
	 * While SIZE
	 * 	number = (number * size) + int value of the char
	 */
	private Integer stringToNumber(String strVal) {
		int numb = 0;
        for (int i = 0; i < strVal.length(); i++) {
            numb = numb * SIZE + CHARS.indexOf(strVal.charAt(i));
        }
        return numb;
	}
	
	
	/**
	 * Get number of access
	 */
	public Integer getStat(String shortUrl, Connection con, StringBuilder message) {
		if (shortUrl == null) {
			message.append("Inform the short URL");
			return -1;
		} else if (con == null) {
			message.append("No connection available. Execute connection first.");
			return -1;
		}
		
		//Split domain from rest URL
		int pos = shortUrl.lastIndexOf('/');
		String domain = shortUrl.substring(0, pos);
		String rest = shortUrl.substring(pos+1, shortUrl.length());
		Integer restNum = stringToNumber(rest);

		String execSQL = "SELECT sht.statistics FROM domain dom, shortenurl sht WHERE dom.domain=? AND sht.shorturl=? AND dom.domain_id=sht.domain_id";
		PreparedStatement ps;
		Integer statistics = -1;

		try {
			ps = con.prepareStatement(execSQL);
			ps.setString(1, domain);
			ps.setInt(2, restNum);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				statistics = rs.getInt(1);
			} else {
				message.append("Short URL not found. Insert the new one.");
			}

			ps.close();
			execSQL = null;
		} catch (SQLException ex) {
			System.out.println(execSQL);
			ex.printStackTrace();
			message.append("Database query error.");
		}
		return statistics;
	}
}
