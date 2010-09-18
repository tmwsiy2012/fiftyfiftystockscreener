package com.eddiedunn.screen;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.NumberFormat;

import com.eddiedunn.data.Stock;
import com.eddiedunn.util.StopWatch;
import com.eddiedunn.util.UC;


public class Screener {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//screenMarketWithFiles(3,4);
		// 1:NASDAQ 2:NYSE 3:AMEX
		//int marketid = 2;
		//UC.emailResults(7);
		//screenMarket(marketid,UC.initializeGatherRun(marketid),0,true);
		}
	
public static int screenMarket(int marketid, int gatherRunID, int startIndex , boolean useOnlySymbolsWithData){
	StopWatch watch = new StopWatch();
	String lastSuccessfulTicker= "";
	int lastSuccessfulIndex=0;
	Stock s = null;
	String[] symbols =null;
	try {
		NumberFormat nf = NumberFormat.getInstance();			
				
		watch.start();
	//String[] symbols = UC.readSymbolsFromFile("C:\\Users\\hpcorei3\\Documents\\screenerdata\\nasdaq4_13_10.csv",",");
	//symbols = UC.readSymbolsFromFile("C:\\Users\\hpcorei3\\Documents\\screenerdata\\NYSE.txt","\t");
	symbols=UC.getSymbolsForGatherRun(gatherRunID,useOnlySymbolsWithData);
	//while( )
	
	for( String str: symbols){
		System.out.println(str);
	}
 
	for (int i = startIndex; i < symbols.length; i++) {			

		s = UC.getData(symbols[i],gatherRunID,marketid,nf, true);
		
		if(! "nodata".equalsIgnoreCase(s.getTicker()) && ! s.isAllZeros()){
			Connection conn=null;
			try {                     
				String q = "INSERT INTO ticker_data (price,operating_cash_flow,qtrly_revenue_growth,qtrly_earnings_growth,shares_outstanding,held_by_institutions,gather_runid,marketid,dateObserved,ticker) VALUES("+s.getCurrentQuote()+","+s.getOperatingCashFlow()+","+s.getQtrlyRevGrowth()+","+s.getQtrlyEarningsGrowth()+","+s.getSharesOutstanding()+","+s.getPercentHeldByInstitutions()+","+gatherRunID+","+marketid+",now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);        			
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				 try {conn.close();} catch (Exception e) {}
			}			
		}
		
		if( s.isInteresting() ){
			Connection conn=null;
			try {
				String q = "INSERT INTO interesting(gather_runid,dateObserved,ticker) VALUES("+gatherRunID+",now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);        			
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				 try {conn.close();} catch (Exception e) {}
			}	
		}
		
		//Thread.sleep(5000);
		lastSuccessfulTicker = s.getTicker();
		lastSuccessfulIndex = i;
	}
	} catch (Exception e) {
		e.printStackTrace();
	}finally{
		
		watch.stop();
		System.out.println("Done\nlastSuccessfulTicker: "+lastSuccessfulTicker+"\nlastSuccessfulIndex: "+lastSuccessfulIndex+" Total Time: "+watch.getElapsedTimeSecs()/60.0+" minutes");
		if( lastSuccessfulIndex == symbols.length -1){
			// completed successfully so lets email the results
			UC.emailResults(gatherRunID); 

			// update gather_run to reflect completion 
			UC.updateGatherRun(gatherRunID,true,lastSuccessfulIndex);
		}else{
			// store last completed index for later resume
			UC.updateGatherRun(gatherRunID,false,lastSuccessfulIndex);
		} 
	}
	return gatherRunID;
			
}
public static void screenMarketWithFiles(int marketid,int gatherRunID){
	
	boolean updateWithData = false;
	
	boolean foundSomething = false; 
	
	FileWriter outFile = null;
	PrintWriter out=null;
	PrintWriter out1=null;
	StopWatch watch = new StopWatch();
	String lastSuccessfulTicker= "";
	int lastSuccessfulIndex=0;
	///////////////////////////////////////////////
	int startIndex=0;/////// LOOK HERE!!!!!
	/////////////////////////////////////////////////////////////////
	Stock s = null;
	String[] symbols =null;
	try {
		NumberFormat nf = NumberFormat.getInstance();
		outFile = new FileWriter("C:\\Users\\hpcorei3\\Documents\\screenerdata\\interesting.csv");
		out = new PrintWriter(outFile);
		FileWriter outFile1 = new FileWriter("C:\\Users\\hpcorei3\\Documents\\screenerdata\\symbolsWithData.csv");
		out1 = new PrintWriter(outFile1);			
		
		
		//ExecutorService pool = Executors.newFixedThreadPool(5);
		
		
		watch.start();
	symbols = UC.readSymbolsFromFile("C:\\Users\\hpcorei3\\Documents\\screenerdata\\AMEX.txt","\t");
	//String[] symbols = UC.readSymbolsFromFile("C:\\Users\\hpcorei3\\Documents\\screenerdata\\OTCBB.txt","\t");
	//while( )
	
	
	
	for (int i = startIndex; i < symbols.length; i++) {			
		if( i % (symbols.length/10) == 0){
			double percent = (new Double(i)).doubleValue()/(new Double(symbols.length)).doubleValue();
			System.out.println("On ticker "+i+" of "+symbols.length+" ("+(percent*100)+"%)");
		}
		s = UC.getData(symbols[i],marketid,gatherRunID,nf, updateWithData);
		//pool.execute(new ScreenSingleStock(symbols[i],nf,i,symbols.length));
		//System.out.println(symbols[i]+" : "+s.getTicker());
		if( s.getTicker().equalsIgnoreCase(symbols[i])){
			//System.out.println(symbols[i]+" has data");
			out1.println(s.getTicker());
		}
		if( s.isInteresting() ){
			foundSomething=true;
			System.out.println("interesting:"+s.getTicker());
			//s.printContents();
			out.println(s.getTicker());
			

			Connection conn=null;
			try {
				String q = "INSERT INTO interesting(gather_runid,dateObserved,ticker) VALUES("+gatherRunID+",now(),'"+s.getTicker()+"');";
			    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";
			    
				
	            Class.forName("com.mysql.jdbc.Driver").newInstance();
	            conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(q);        			
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				 try {conn.close();} catch (Exception e) {}
			}	
			
		}
		
		//Thread.sleep(5000);
		lastSuccessfulTicker = s.getTicker();
		lastSuccessfulIndex = i;
	}

	} catch (Exception e) {
		e.printStackTrace();
	}finally{
		watch.stop();
		if( lastSuccessfulIndex == symbols.length -1){			
			// update gather_run to reflect completion 
			UC.updateGatherRun(gatherRunID,true,lastSuccessfulIndex);
			if( foundSomething )
				UC.emailResults(gatherRunID);
		}else{
			// store last completed index for later resume
			UC.updateGatherRun(gatherRunID,true,lastSuccessfulIndex);
		}		
		System.out.println("Done\nlastSuccessfulTicker: "+lastSuccessfulTicker+"\nlastSuccessfulIndex: "+lastSuccessfulIndex+" Total Time: "+watch.getElapsedTimeSecs()/60.0+" minutes");
		out.close();
		out1.close();			
	}	
}
}
