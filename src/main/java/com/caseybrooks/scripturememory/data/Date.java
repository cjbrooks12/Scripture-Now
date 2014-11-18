package com.caseybrooks.scripturememory.data;

import java.util.Calendar;

public class Date {
	Calendar calendarDate;
	//Date Fields
	int month;
	int day;
	int dayOfWeek;
	int year;
	
	//Time Fields
	int hour;
	int minute;
	int second;
	int millisecond;
	
//Constructors
//------------------------------------------------------------------------------
	public Date() {
		calendarDate = Calendar.getInstance();
		calculateDate();
		calculateTime();
	}

	public Date(Calendar calendarDate) {
		this.calendarDate = calendarDate;
		calculateDate();	
		calculateTime();
	}

	public Date(int month, int day, int year) {
		calendarDate.set(year, month, day);
		calculateDate();
		calculateTime();
	}
	
	private void calculateDate() {
		month = calendarDate.get(Calendar.MONTH);
		day = calendarDate.get(Calendar.DATE);
		dayOfWeek = calendarDate.get(Calendar.DAY_OF_WEEK);
		year = calendarDate.get(Calendar.YEAR);
	}
	
	private void calculateTime() {
		hour = calendarDate.get(Calendar.HOUR_OF_DAY);
		minute = calendarDate.get(Calendar.MINUTE);
		second = calendarDate.get(Calendar.SECOND);
		millisecond = calendarDate.get(Calendar.MILLISECOND);
	}
	
//Public Getters
//------------------------------------------------------------------------------
//Get General Calendar Information
//------------------------------------------------------------------------------
	public String toString() {
		return getDateString() + " " + getTimeString();
	}
	
	public String getDateString() {
		return getDayOfWeek() + " " + getMonth() + " " + day + ", " + year;
	}
	
	public String getTimeString() {
		return hour + ":" + minute + ":" + second;
	}
	
	public Calendar getCalendar() {
		return calendarDate;
	}
	
	public long getSystemMillis() {
		return calendarDate.getTimeInMillis();
	}

//Get Date Information
//------------------------------------------------------------------------------
	public String getDayOfWeek() {
		switch(dayOfWeek) {
		case 1: return "Sunday";
		case 2: return "Monday";
		case 3: return "Tuesday,";
		case 4: return "Wednesday,";
		case 5: return "Thursday";
		case 6: return "Friday";
		case 7: return "Saturday";
		default: return "error";
		}
	}
	
	public String getMonth() {
		switch(month) {
		case 0: return "January";
		case 1: return "February";
		case 2: return "March";
		case 3: return "April";
		case 4: return "May";
		case 5: return "June";
		case 6: return "July";
		case 7: return "August";
		case 8: return "September";
		case 9: return "October";
		case 10: return "November";
		case 11: return "December";
		default: return "error";
		}
	}
	
	public int getDay() {
		return day;
	}
	
	public int getYear() {
		return year;
	}
	
//Get Time Information
//------------------------------------------------------------------------------	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public int getSecond() {
		return second;
	}
	
	public int getMillisecond() {
		return millisecond;
	}
	
//Public Setters
//------------------------------------------------------------------------------
//General Setters 
//------------------------------------------------------------------------------
	public void setCalendar(Calendar calendar) {
		this.calendarDate = calendar;
		calculateDate();
		calculateTime();
	}
	
	public void setSystemMillis(long millis) {
		this.calendarDate.setTimeInMillis(millis);
		calculateDate();
		calculateTime();
	}
	
//Date Setters
//------------------------------------------------------------------------------
	//Parses a string for the name of the month or the number of the month, with January = 1
	public boolean setMonth(String month) {
		if(month.matches("[Jj]an(uary)?|1")) {
			calendarDate.set(Calendar.MONTH, 0);
		}
		else if(month.matches("[Ff]eb(ruary)?|2")) {
			calendarDate.set(Calendar.MONTH, 1);
		}
		else if(month.matches("[Mm]ar(ch)?|3")) {
			calendarDate.set(Calendar.MONTH, 2);
		}
		else if(month.matches("[Aa]pr(il)?|4")) {
			calendarDate.set(Calendar.MONTH, 3);
		}
		else if(month.matches("[Mm]ay|5")) {
			calendarDate.set(Calendar.MONTH, 4);
		}
		else if(month.matches("[Jj]un(e)?|6")) {
			calendarDate.set(Calendar.MONTH, 5);
		}
		else if(month.matches("[Jj]ul(y)?|7")) {
			calendarDate.set(Calendar.MONTH, 6);
		}
		else if(month.matches("[Aa]ug(ust)?|8")) {
			calendarDate.set(Calendar.MONTH, 7);
		}
		else if(month.matches("[Ss]ept(ember)?|9")) {
			calendarDate.set(Calendar.MONTH, 8);
		}
		else if(month.matches("[Oo]ct(ober)?|10")) {
			calendarDate.set(Calendar.MONTH, 9);
		}
		else if(month.matches("[Nn]ov(ember)?|11")) {
			calendarDate.set(Calendar.MONTH, 10);
		}
		else if(month.matches("[Dd]ec(ember)?|12")) {
			calendarDate.set(Calendar.MONTH, 11);
		}
		else return false;
		calculateDate();
		return true;
	}
	
	public void setDay(int day) {
		calendarDate.set(Calendar.DATE, day);
		calculateDate();
	}
	
	public void setYear(int year) {
		calendarDate.set(Calendar.YEAR, year);
		calculateDate();
	}
	
//Time Setters
//------------------------------------------------------------------------------
	public void setHour(int hour) {
		calendarDate.set(Calendar.HOUR_OF_DAY, hour);
		calculateTime();
	}
	
	public void setMinute(int minute) {
		calendarDate.set(Calendar.MINUTE, minute);
		calculateTime();
	}
	
	public void setSecond(int second) {
		calendarDate.set(Calendar.SECOND, second);
		calculateTime();
	}
	
	public void setMillisecond(int millisecond) {
		calendarDate.set(Calendar.MILLISECOND, millisecond);
		calculateTime();
	}
}