package com.eddiedunn.data;

public class GatherRunResult {

	private int gatherRunID;
	private int marketID;
	private String marketName;
	private String csv;
	private String[] tickers;
	
	public GatherRunResult(int gatherRunID,int marketID,String marketName, String csv, String[] tickers){
		this.gatherRunID = gatherRunID;
		this.marketID = marketID;
		this.marketName = marketName;		
		this.csv = csv;
		this.tickers = tickers;
	}

	public int getGatherRunID() {
		return gatherRunID;
	}

	public int getMarketID() {
		return marketID;
	}

	public String getMarketName() {
		return marketName;
	}

	public String getCSV() {
		return csv;
	}

	public String[] getTickers() {
		return tickers;
	}


	
}
