package com.eddiedunn.screen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.eddiedunn.util.UC;

public class DealWithNoData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// for each market
		for (int i = 1; i <= 3; i++) {
			// grab tickers with nodata and try to deal with
			String sql = "select ticker from tickers where marketid="+i+" AND ticker NOT IN (select ticker from withdata where marketid="+i+");";
			Connection conn=null;
			
		    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";	    
			try {
		        Class.forName("com.mysql.jdbc.Driver").newInstance();
		        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
				Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);

				ResultSet srs = stmt.executeQuery(sql);

				while (srs.next()) {					
					String ticker = srs.getString("ticker").trim();
					UC.evaluateNoDataSymbol(ticker);
				}		
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					//e.printStackTrace();
			}
			}			
		}
		
	}

}
