package com.eddiedunn.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

	//public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	  public static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());

	  }

	  public static void  main(String arg[]) {
	     System.out.println(DateUtils.now("dd MMMMM yyyy"));
	     System.out.println(DateUtils.now("yyyy.MM.dd"));
	     System.out.println(DateUtils.now("dd.MM.yy"));
	     System.out.println(DateUtils.now("MM/dd/yy"));
	     System.out.println(DateUtils.now("MM/dd/yyyy 'at' hh:mm:ss z"));
	     System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
	     System.out.println(DateUtils.now("h:mm a"));
	     System.out.println(DateUtils.now("H:mm:ss:SSS"));
	     System.out.println(DateUtils.now("K:mm a,z"));
	     System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
	  }
	}

