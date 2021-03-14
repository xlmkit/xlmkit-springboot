package com.xlmkit.springboot.support.util;

import java.util.Date;


public class DateUtils {
	public static void timeStart(Date time) {
		if(time==null) {
			return;
		}
		time.setHours(0);
		time.setSeconds(0);
		time.setMinutes(0);
	}
	public static void timeEnd(Date time) {
		if(time==null) {
			return;
		}
		time.setHours(23);
		time.setSeconds(59);
		time.setMinutes(59);
	}
}
