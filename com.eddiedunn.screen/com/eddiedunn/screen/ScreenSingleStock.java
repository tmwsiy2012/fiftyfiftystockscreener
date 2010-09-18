package com.eddiedunn.screen;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eddiedunn.data.Stock;
import com.eddiedunn.util.StopWatch;
import com.eddiedunn.util.UC;



public class ScreenSingleStock implements Runnable {
	
    int id;
    int totalTickers;
    String ticker;
    NumberFormat nf;
    PrintWriter out;
    StopWatch watch;
    ScreenSingleStock(String ticker, NumberFormat nf, int id,int totalTickers) {
        this.ticker = ticker;
        this.nf = nf;
        this.id = id;
        this.totalTickers=totalTickers;
        this.out=out;
    }

    public void run(){
    	try {
    		watch = new StopWatch();
    		watch.start();
			if( id % 100 == 0)
				System.out.println("processed "+id+" of of "+totalTickers+" current symbol: "+ticker);
		Stock s = new Stock(ticker);
		
		String page = UC.getPage("http://finance.yahoo.com/q/ks?s="+s.getTicker());
		//System.out.println(page);
		if( page.contains("There is no  data available for "+s.getTicker()) ||
				page.contains("<title>Invalid Ticker Symbol") ||
				page.contains("There are no All Markets results for") ||
				page.contains("<title>Symbol Lookup"))
			throw new Exception("Symbol Not Found");
		
		//else
		//	System.out.println(page);
		//PATTERN: Qtrly Revenue Growth (yoy):</td><td class="yfnc_tabledata1">15.00%</td>
		
		Pattern patt = Pattern.compile("Qtrly\\s+Revenue\\s+Growth\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");			
		Matcher m = patt.matcher(page);

		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Qtrly Revenue Growth");
			s.setQtrlyRevGrowth(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find Qtrly Revenue Growth");
		}		
		
		
		patt = Pattern.compile("Qtrly\\s+Earnings\\s+Growth\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Earnings Growth");		
			s.setQtrlyEarningsGrowth(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find Earnings Growth");
		}
		
		// Operating Cash Flow
		patt = Pattern.compile("Operating\\s+Cash\\s+Flow\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Operating Cash Flow");			
			double val = nf.parse(data.substring(0, data.length()-1)).doubleValue();
			if( data.endsWith("M"))
				val *= 1000000;
			if( data.endsWith("B"))
				val *= 1000000000;		
			
			s.setOperatingCashFlow(val);
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find Operating Cash Flow");
		}		
		
		// Shares Outstanding<font size="-1"><sup>5</sup></font>:</td><td class="yfnc_tabledata1">
		patt = Pattern.compile("Shares\\s+Outstanding<font size=\"-1\"><sup>5</sup></font>:</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Shares Outstanding");
			double val =  nf.parse(data.substring(0, data.length()-1)).doubleValue();

			if( data.endsWith("M"))
				val *= 1000000;
			if( data.endsWith("B"))
				val *= 1000000000;		
			
			s.setSharesOutstanding(val);
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find Shares Outstanding");
		}		
		
		// Held by Institutions<font size="-1"><sup>1</sup></font>:</td><td class="yfnc_tabledata1">

		patt = Pattern.compile("Held by Institutions<font size=\"-1\"><sup>1</sup></font>:</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Held by Institutions");		
			s.setPercentHeldByInstitutions(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find held by institution");
		}
		// 
		page = UC.getPage("http://finance.yahoo.com/q?s="+s.getTicker());
		patt = Pattern.compile("Prev\\s+Close:</th><td\\s+class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A"))
				throw new Exception("ERROR N/A for Price");				
			s.setCurrentQuote(nf.parse(data).doubleValue());
			//System.out.println(data);
		}else{
			throw new Exception("ERROR could not find previous price");
		}
		
		//System.out.println(s.getTicker());
		
		// if we get here then we have everything we need to evaluate the results and log its a valid, complete symbol
		if( s.isInteresting() ){
			//out.println(s.getTicker());
			System.out.println("Interesting: "+s.getTicker());// log it as interesting
			Connection conn=null;
			try {
		        ResultSet rst = null;

				String q = "INSERT INTO interesting(dateObserved,ticker) VALUES(now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);
	            //rst = stmt.executeQuery(q);         			
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				 try {conn.close();} catch (Exception e) {}
			}

			
		}else{
			Connection conn=null;
			try {
		        ResultSet rst = null;

				String q = "INSERT INTO withdata(dateObserved,ticker) VALUES(now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);
	            //rst = stmt.executeQuery(q);         			
			} catch (Exception e) {
				e.printStackTrace();
			}	finally{
				 try {conn.close();} catch (Exception e) {}
			}
		
		}
			
		//selse
			//System.out.println("Data"+s.getTicker());
		
    	} catch (Exception e) {
    		// TODO: Log exception information 
    		//System.out.println("Error: "+e.getMessage());
    	
    		
    	}  	
	}
  	
    
	

}
