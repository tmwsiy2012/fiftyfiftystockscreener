package com.eddiedunn.data;

public class Stock {


	String ticker;
	double qtrlyRevGrowth;
	double qtrlyEarningsGrowth;
	double operatingCashFlow;
	double sharesOutstanding;
	double percentHeldByInstitutions;
	double currentQuote;
	
	public boolean isAllZeros(){
		if( isZero(qtrlyEarningsGrowth)  && isZero(qtrlyRevGrowth)  &&
				isZero(operatingCashFlow) && isZero(sharesOutstanding) && isZero(percentHeldByInstitutions))
			return true;
		
		return false;
	}
	private boolean isZero(double testVal){
		double variance = 0.00000001;
		if( testVal > -variance && testVal < variance)
			return true;
		
		return false;
	}
	public Stock(String Ticker){
		ticker = Ticker;
	}

	
	/*
	 * 
	 * 
Income Statement
    Qtrly revenue growth yoy > 25%
    qtrly earnings growth yoy > 25%
Balance Sheet
    operating cash flow > 0
Share statistics
    Shares outstanding < 100,000,000
    % held by institutions > 20%

Price
    between 10 - 40
	 */
	
	public boolean isInteresting(){
		boolean retval = false;
		if( isInterestingWithoutPrice() &&
			currentQuote >= 9.5 &&
			currentQuote <= 42 
		)
			retval = true;
		
		return retval;
	}
	
	public boolean isInterestingWithoutPrice(){
		boolean retval = false;
		if( qtrlyRevGrowth >= 23.75 &&
			qtrlyEarningsGrowth >= 23.75 &&	
			operatingCashFlow > 0 &&
			sharesOutstanding < 100000000 &&
			percentHeldByInstitutions > 20 &&
			! ticker.equalsIgnoreCase("nodata")
		)
			retval = true;
		
		return retval;
	}
	
	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public double getQtrlyRevGrowth() {
		return qtrlyRevGrowth;
	}

	public void setQtrlyRevGrowth(double qtrlyRevGrowth) {
		this.qtrlyRevGrowth = qtrlyRevGrowth;
	}



	public double getQtrlyEarningsGrowth() {
		return qtrlyEarningsGrowth;
	}


	public void setQtrlyEarningsGrowth(double qtrlyEarningsGrowth) {
		this.qtrlyEarningsGrowth = qtrlyEarningsGrowth;
	}


	public double getOperatingCashFlow() {
		return operatingCashFlow;
	}

	public void setOperatingCashFlow(double operatingCashFlow) {
		this.operatingCashFlow = operatingCashFlow;
	}

	public double getSharesOutstanding() {
		return sharesOutstanding;
	}

	public void setSharesOutstanding(double sharesOutstanding) {
		this.sharesOutstanding = sharesOutstanding;
	}

	public double getPercentHeldByInstitutions() {
		return percentHeldByInstitutions;
	}

	public void setPercentHeldByInstitutions(double percentHeldByInstitutions) {
		this.percentHeldByInstitutions = percentHeldByInstitutions;
	}

	public double getCurrentQuote() {
		return currentQuote;
	}

	public void setCurrentQuote(double currentQuote) {
		this.currentQuote = currentQuote;
	}

	public void printContents(){
		System.out.println("Ticker: "+ticker);
		System.out.println("qtrlyRevGrowth: "+qtrlyRevGrowth);
		System.out.println("qtrlyEarningsGrowth: "+qtrlyEarningsGrowth);
		System.out.println("operatingCashFlow: "+operatingCashFlow);
		System.out.println("sharesOutstanding: "+sharesOutstanding);
		System.out.println("percentHeldByInstitutions: "+percentHeldByInstitutions);
		System.out.println("currentQuote: "+currentQuote);		
			
	}
}
