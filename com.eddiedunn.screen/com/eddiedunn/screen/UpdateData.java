package com.eddiedunn.screen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.eddiedunn.util.UC;


public class UpdateData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// first check for any incomplete gather_runs and try to complete
		completeStalledRuns();

		
		// then run all markets 
		
		for (int marketid = 1; marketid <= 3; marketid++) {
			Screener.screenMarket(marketid,UC.initializeGatherRun(marketid),0,true);
		}
	}
	private static void reRunBatches(int numDays){
		String unFinishedsql = "select gather_runid,marketid,last_completed_id from gather_runs where is_completed=1 and ADDDATE(time_completed, INTERVAL 1 DAY) < now();";
		
		Connection conn=null;
		
	    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";	    
		try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");		
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			ResultSet srs = stmt.executeQuery(unFinishedsql);


			while (srs.next()) {
				Integer gatherRunID = srs.getInt("gather_runid");
				Integer marketid = srs.getInt("marketid");
				Integer lastCompletedID = srs.getInt("last_completed_id");
				
				Screener.screenMarket(marketid, gatherRunID, lastCompletedID, true);
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
private static void completeStalledRuns(){
	String unFinishedsql = "select gather_runid,marketid,last_completed_id from gather_runs where is_completed=0;";
	
	Connection conn=null;
	
    final String cURL = "jdbc:mysql://192.168.5.12:3306/screener";	    
	try {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(cURL, "tmwsiy", "tr45sh32");		
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
		ResultSet srs = stmt.executeQuery(unFinishedsql);


		while (srs.next()) {
			Integer gatherRunID = srs.getInt("gather_runid");
			Integer marketid = srs.getInt("marketid");
			Integer lastCompletedID = srs.getInt("last_completed_id");
			
			Screener.screenMarket(marketid, gatherRunID, lastCompletedID, true);
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
