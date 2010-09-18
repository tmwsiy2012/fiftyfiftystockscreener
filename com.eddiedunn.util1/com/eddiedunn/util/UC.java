package com.eddiedunn.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eddiedunn.data.GatherRunResult;
import com.eddiedunn.data.Stock;

public class UC {
	public static void writeOneLetterOfNasdaqSymbols(PrintWriter out, String letter){
		try {
		
			String url1 = "http://www.allstocks.com/NASDAQ/nasdaq_stock_symbols_";
			String url2=".html";
							
			String page = getPage(url1+letter+url2);
			
			String expr = "FACE=\"MS Sans Serif\">([^<]+)";
			
			Pattern patt = Pattern.compile(expr);			
			Matcher m = patt.matcher(page);
			int count = 0;
			String ticker = "";
			String companyName="";
			while (m.find()) {
				String data = m.group(1);
				//System.out.println( data);				
				if( count % 3 == 0){
					// symbol
					out.print(data);
					ticker=data;
				}
				if( count % 3 == 1){
					// Company Name
					out.println(",\""+ data+"\"");
					companyName=data;
					writeNasdaqTicker(ticker,companyName);
				}
				if( count % 3 == 2){
					// not needed
				}				

			  count++;
			}
		
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}		
	}
	public static void writeNasdaqTicker(String ticker, String companyName){
		Connection conn=null;
		try {
			String q = "INSERT INTO nasdaq(ticker,companyName) VALUES('"+ticker+"','"+companyName.replaceAll("'", "''")+"');";
		    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
		    
			
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(cURL, "tmwsiy", "password");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(q);         			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			 try {conn.close();} catch (Exception e) {}
		}	
	}
	public static void getKeyStatistics(){
		
	}
	public static synchronized  String getPage(String urlStr){
		String page="";
		try {
			URL url = new URL(urlStr);
			
 
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); 
			String str; 
			StringBuffer pageBuf = new StringBuffer();
			while ((str = in.readLine()) != null) { 
					pageBuf.append(str);
			}
			page = pageBuf.toString();
			
			in.close(); 
		} catch (Exception e) {
			e.printStackTrace();
			
		}	
		return page;
	}
	public static String[] readValidSymbolsFromDatabase(String market){
		return new String[1];
	}
	public static void updateGatherRun(int gatherRunID, boolean isFinished, int lastCompleteIndex){
		Connection conn=null;
		try {
			String q = "";
			if( isFinished  )
				q= "UPDATE gather_runs SET is_completed=1,time_completed=now()  WHERE gather_runid="+gatherRunID+";";
			else
				q= "UPDATE gather_runs SET last_completed_id="+lastCompleteIndex+"  WHERE gather_runid="+gatherRunID+";";
		    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
		    
			
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(cURL, "tmwsiy", "password");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(q);        			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			 try {conn.close();} catch (Exception e) {}
		}		
	}
	public static GatherRunResult getGatherRunResult(int gatherRunID){
		String csv = getGatherRunInterestingDataRows(gatherRunID);
		String data = getGatherRunDataRows(gatherRunID);
		Connection conn=null;
		String marketName = "";
		Integer marketID = null;
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";		
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = 
				"select m.marketName,m.marketid from gather_runs gr "+
				"inner join markets m on m.marketid=gr.marketid "+
				"where gather_runid="+gatherRunID+";";



			ResultSet srs = stmt.executeQuery(sql);


			if (srs.next()) {
				marketName = srs.getString("marketName");
				marketID = srs.getInt("marketid");
				//System.out.print(marketName+" "+marketID.intValue());
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
		return new GatherRunResult(gatherRunID, marketID.intValue(), marketName, csv, getGatherTickers(gatherRunID));	
	}
	public static String[] getGatherTickers(int gatherRunID){
		Connection conn=null;
		
		ArrayList<String> ar = new ArrayList<String>();
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";	    
	    	    
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = 
				"select i.ticker from interesting i WHERE " + "i.gather_runid="+gatherRunID+" ORDER BY i.ticker;";

			ResultSet srs = stmt.executeQuery(sql);


			while (srs.next()) {
				String ticker = srs.getString("ticker").trim();
				ar.add(ticker);
				
				System.out.println(ticker);
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
		return (String []) ar.toArray (new String [ar.size ()]);
		
	}		
	public static void evaluateNoDataSymbol(String ticker){
		String page = getPage("http://finance.yahoo.com/lookup?s="+ticker);
		if( page.contains("There is no  data available for "+ticker)){
			System.out.println("No data avail: "+ticker);
		}
		else if( page.contains("<title>Invalid Ticker Symbol")){
			System.out.println("invalid ticker: "+ticker);
		}
		else if( page.contains("Changed Ticker Symbol")){
			System.out.println("changed ticker: "+ticker);
		}
		else if( page.contains("There are no All Markets results for") ){
			//System.out.println("no all market: "+ticker);
		}else{
			System.out.println("something else going on: "+ticker);
			System.out.println(page);
		}
		
		
		if( page.contains("<title>Symbol Lookup")){
			//System.out.println("symbol lookup: "+ticker);
		}
		
		
	}
	public static String getGatherRunDataRows(int gatherRunID){
		Connection conn=null;
		String pattern = "yyyy.MM.dd 'at' HH:mm:ss z";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
	    StringBuffer output = new StringBuffer();
	    output.append("\"Market Name\",\"Ticker\",\"Company Name\",\"Interactive Chart\",\"Date Pulled\"\n");	    
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = 
				"select m.marketName,i.ticker,t.companyName,i.dateObserved from interesting i " +
					"inner join gather_runs gr on gr.gather_runid=i.gather_runid " +
					"inner join markets m on m.marketid=gr.marketid " +
					"INNER JOIN tickers t on t.ticker=i.ticker " +
					"WHERE " +
					"i.gather_runid="+gatherRunID+
					" ORDER BY i.ticker;";

			ResultSet srs = stmt.executeQuery(sql);


			while (srs.next()) {
				String marketName = srs.getString("marketName").trim();
				String ticker = srs.getString("ticker").trim();
				String companyName = srs.getString("companyName").trim();
				
				Timestamp dateObserved = srs.getTimestamp("dateObserved");
				output.append("\""+marketName+"\",\""+ticker+"\",\""+companyName+"\",\"http://finance.yahoo.com/echarts?s=CPK+Interactive#symbol="+ticker+"\",\""+sdf.format(dateObserved)+"\"\n");
				System.out.print("\""+marketName+"\",\""+ticker+"\",\""+companyName+"\",\"http://finance.yahoo.com/echarts?s=CPK+Interactive#symbol="+ticker+"\",\""+sdf.format(dateObserved)+"\"\n");
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
		return output.toString();
		
	}		
	public static String getGatherRunInterestingDataRows(int gatherRunID){
		Connection conn=null;
		String pattern = "yyyy.MM.dd 'at' HH:mm:ss z";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
	    StringBuffer output = new StringBuffer();
	    output.append("\"Market Name\",\"Ticker\",\"Company Name\",\"Interactive Chart\",\"Date Pulled\"\n");	    
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = 
				"select m.marketName,i.ticker,t.companyName,i.dateObserved from interesting i " +
					"inner join gather_runs gr on gr.gather_runid=i.gather_runid " +
					"inner join markets m on m.marketid=gr.marketid " +
					"INNER JOIN tickers t on t.ticker=i.ticker " +
					"WHERE " +
					"i.gather_runid="+gatherRunID+
					" ORDER BY i.ticker;";

			ResultSet srs = stmt.executeQuery(sql);


			while (srs.next()) {
				String marketName = srs.getString("marketName").trim();
				String ticker = srs.getString("ticker").trim();
				String companyName = srs.getString("companyName").trim();
				
				Timestamp dateObserved = srs.getTimestamp("dateObserved");
				output.append("\""+marketName+"\",\""+ticker+"\",\""+companyName+"\",\"http://finance.yahoo.com/echarts?s=CPK+Interactive#symbol="+ticker+"\",\""+sdf.format(dateObserved)+"\"\n");
				System.out.print("\""+marketName+"\",\""+ticker+"\",\""+companyName+"\",\"http://finance.yahoo.com/echarts?s=CPK+Interactive#symbol="+ticker+"\",\""+sdf.format(dateObserved)+"\"\n");
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
		return output.toString();
		
	}	
	public static int initializeGatherRun(int marketid){
		Connection conn=null;
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = "INSERT INTO gather_runs (marketid, " +
					"time_started," +
					" time_completed," +
					" is_completed," +
					" last_completed_id," +
					" isStarted)" +
					" VALUES("+marketid+",now(),null,0,0,1);";
			
			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = null;
		    int autoIncKeyFromApi = -1;

		    rs = stmt.getGeneratedKeys();

		    if (rs.next()) {
		        autoIncKeyFromApi = rs.getInt(1);
		    } else {
		        throw new Exception("Error inserting new gather Run");		        
		    }

		    rs.close();

		    rs = null;
		    return autoIncKeyFromApi;
		
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				//e.printStackTrace();
		}}		
		return -1;
	}
	public static String[] getSymbolsForGatherRun(int gatherRunID, boolean onlyWithData){
		Connection conn=null;
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
	    ArrayList<String> ar = new ArrayList<String>();
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "password");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			ResultSet srs = null;
			if( onlyWithData)
				srs = stmt.executeQuery("SELECT tickers.ticker FROM tickers INNER JOIN gather_runs on gather_runs.marketid=tickers.marketid WHERE gather_runs.gather_runid="+gatherRunID+" AND tickers.ticker IN (SELECT withdata.ticker FROM withdata INNER JOIN gather_runs on withdata.marketid=gather_runs.marketid WHERE gather_runs.gather_runid="+gatherRunID+");");
			else  
				srs = stmt.executeQuery("SELECT t.ticker FROM tickers t INNER JOIN gather_runs grs on t.marketid=grs.marketid WHERE grs.gather_runid="+gatherRunID);
			while (srs.next()) {
				String name = srs.getString("ticker");
				ar.add(name);
				System.out.println(name);
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
		return (String []) ar.toArray (new String [ar.size ()]);
		
	}
	public static String[] readSymbolsFromFile(String filename, String delimiter){
		File file = new File(filename);
		BufferedReader reader = null;
		ArrayList<String> ar = new ArrayList<String>();
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			
			// skip header row
			reader.readLine();
			
			// repeat until all lines are read
			while ((text = reader.readLine()) != null) {
				String[] tmp = text.split(delimiter);
				ar.add(tmp[0].trim());
				System.out.println(tmp[0].trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return (String []) ar.toArray (new String [ar.size ()]);
	}
	public static String getDataString(String dataFile) {
		File file = new File(dataFile);
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			// repeat until all lines are read
			while ((text = reader.readLine()) != null) {
				// if( text.length() > 0)
				contents.append(text).append(" ");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return contents.toString();

	}	
	public static void emailResults(int gatherRunID){
		
		// first build up the data
		
		GatherRunResult grr = getGatherRunResult(gatherRunID);
		
		String body = getEmailBody(grr);
		
		String subject = "screener results for "+grr.getMarketName()+" completed: "+DateUtils.now("MM/dd/yyyy 'at' hh:mm:ss z");
		String fromAddress = "eddie@eddiedunn.com";
		String toAddresses = "tmwsiy2012@gmail.com;eddunn@triad.rr.com";		
		String smtpServer = "smtp-server.ec.rr.com";
		
		//send the message
		SendMail.sendEmail(smtpServer, fromAddress, toAddresses, subject, body, grr.getCSV(), grr.getMarketName());
		
	}

	public static String getEmailBody(GatherRunResult grr){
		StringBuffer str = new StringBuffer(); 
		str.append("<html><head></head><body>");
		str.append("<h3>Results for "+grr.getMarketName()+"</h3>");
		str.append("<table>");
		for (String ticker: grr.getTickers()) {
			str.append("<tr><td>"+ticker+"</td><td><a href=\"http://www.google.com/finance?chdnp=1&chdd=1&chds=1&chdv=0&chvs=maximized&chdeh=1&chfdeh=0&chdet=1277342413361&chddm=493833&chls=IntervalBasedLine&q="+ticker+"&ntsp=0"+"\">Chart</a></td></tr>");
		}
		
		str.append("</table></body></html>");
		return str.toString();
	}
	public static Stock getData(String ticker,int marketid,int gatherRunID, NumberFormat nf, boolean updateWithData) throws Exception{
		Stock s = new Stock(ticker);
		
		String page = UC.getPage("http://finance.yahoo.com/q/ks?s="+s.getTicker());
		//System.out.println(page);
		if( page.contains("There is no  data available for "+s.getTicker()) ||
				page.contains("<title>Invalid Ticker Symbol") ||
				page.contains("There are no All Markets results for") ||
				page.contains("<title>Symbol Lookup"))
		{return new Stock("nodata");}
		else{
			if( updateWithData ){
			Connection conn=null;
			try {
				String q = "INSERT INTO withdata(gather_runid,marketid,dateObserved,ticker) VALUES("+gatherRunID+","+marketid+",now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "password");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);
	            //rst = stmt.executeQuery(q);         			
			} catch (Exception e) {
				e.printStackTrace();
			}	finally{
				 try {conn.close();} catch (Exception e) {}
			}	}					
		}
		//else
		//	System.out.println(page);
		//PATTERN: Qtrly Revenue Growth (yoy):</td><td class="yfnc_tabledata1">15.00%</td>
		
		Pattern patt = Pattern.compile("Qtrly\\s+Revenue\\s+Growth\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");			
		Matcher m = patt.matcher(page);

		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");
			s.setQtrlyRevGrowth(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find Qtrly Revenue Growth");
		}		
		
		
		patt = Pattern.compile("Qtrly\\s+Earnings\\s+Growth\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");			
			s.setQtrlyEarningsGrowth(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find qrely earnings growth");
		}
		
		// Operating Cash Flow
		patt = Pattern.compile("Operating\\s+Cash\\s+Flow\\s+.{6}</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");		
			double val =0;
			if(! data.equals("0"))
				val = nf.parse(data.substring(0, data.length()-1)).doubleValue();
			if( data.endsWith("M"))
				val *= 1000000;
			if( data.endsWith("B"))
				val *= 1000000000;		
			
			s.setOperatingCashFlow(val);
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find operating cash flow");
		}		
		
		// Shares Outstanding<font size="-1"><sup>5</sup></font>:</td><td class="yfnc_tabledata1">
		patt = Pattern.compile("Shares\\s+Outstanding<font size=\"-1\"><sup>5</sup></font>:</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");
			double val =0;
			if(! data.equals("0"))
				val = nf.parse(data.substring(0, data.length()-1)).doubleValue();

			if( data.endsWith("M"))
				val *= 1000000;
			if( data.endsWith("B"))
				val *= 1000000000;		
			
			s.setSharesOutstanding(val);
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find shares outstanding");
		}		
		
		// Held by Institutions<font size="-1"><sup>1</sup></font>:</td><td class="yfnc_tabledata1">

		patt = Pattern.compile("Held by Institutions<font size=\"-1\"><sup>1</sup></font>:</td><td class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");			
			s.setPercentHeldByInstitutions(nf.parse(data.substring(0, data.length()-1)).doubleValue());
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find held by institution");
		}
		// 
		page = UC.getPage("http://finance.yahoo.com/q?s="+s.getTicker());
		patt = Pattern.compile("Prev\\s+Close:</th><td\\s+class=\"yfnc_tabledata1\">([^<]+)");
		m = patt.matcher(page);
		
		if (m.find()) {
			String data = m.group(1);
			if(data.contains("N/A") || data.contains("Na") || data.contains("NA") || data.length() == 0)
				return new Stock("nodata");			
			s.setCurrentQuote(nf.parse(data).doubleValue());
			//System.out.println(data);
		}else{
			//System.out.println("ERROR could not find current price");
		}
		
		return s;
	}

}
