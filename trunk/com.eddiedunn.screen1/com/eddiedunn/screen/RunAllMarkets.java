package com.eddiedunn.screen;

import com.eddiedunn.util.UC;

public class RunAllMarkets {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int marketid = 1; marketid <= 3; marketid++) {
			Screener.screenMarket(marketid,UC.initializeGatherRun(marketid),0,true);			
		}
	}

}
