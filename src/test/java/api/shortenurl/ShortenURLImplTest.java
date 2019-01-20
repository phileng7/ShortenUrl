package api.shortenurl;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;

public class ShortenURLImplTest {
	
	ShortenURL shortUrl;
	Connection con;
	
	@Before
	public void init() {
		shortUrl = new ShortenURLImpl();
	}
	
	@Test
	public void connectionTest() {
		StringBuilder res = new StringBuilder();
		con = shortUrl.connection(null, "mysql", res);
		System.out.println(res);
		assertEquals("db.properties file path argument missing", res);
		res.setLength(0);
		
		con = shortUrl.connection("/temp", null, res);
		System.out.println(res);
		assertEquals("prefix found in db.properties file argument missing", res);
		res.setLength(0);
		
		con = shortUrl.connection("/user", "mysql", res);
		System.out.println(res);
		assertEquals("db.properties file not found", res);
		res.setLength(0);
	}
	

}
