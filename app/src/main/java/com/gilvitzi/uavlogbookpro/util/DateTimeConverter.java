package com.gilvitzi.uavlogbookpro.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * DateTime Configuration:
 * Date is saved in the DB as ISO8601
 * 
 * 
 */
@SuppressLint("SimpleDateFormat")
public class DateTimeConverter {
	public static final String LOG_TAG = "DateTimeConverter";
	
	public static final String ISO8601 = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_DOTTED = "dd.MM.yyyy";
	public static final String DATE_SLASHED = "dd/MM/yyyy";
	
	public static final String DATE_US = "MM/dd/yyyy";
	public static final String DATE_DEFAULT = "dd.MM.yyyy";
	public static final String DATE_DEFAULT_SHORT = "d.M.yy";
	
	public static final String YEAR_MONTH_NUMERIC = "yyyy-MM";
	public static final String YEAR_MONTH_TEXTUAL_SHORT = "yyyy-MMM";
	public static final String YEAR_MONTH_TEXTUAL_LONG = "yyyy-MMMM";
	public static final String DURATION_DEFAULT = "HH:mm";
	public static final String DURATION_DECIMAL = "HH.FF";
	public static final String DURRATION_HTML5 = "PTHHHmmM";
	
	public static final int NUMERIC_UNIX_APPOC = 0; //MilliSeconds Since 1.1.1970
	public static final int NUMERIC_JULIAN_DAY = 1; //the number of days since noon in Greenwich on November 24, 4714 B.C. according to the proleptic Gregorian calendar
	
	public static final Locale LOCAL_DEFAULT = Locale.ENGLISH;
	
	
    public static String dateFormat(String date,String inputFormat,String outputFormat){
		//Date String format converter
		String newDate = null;
	    try{

	        SimpleDateFormat sdf_in = new SimpleDateFormat(inputFormat);
	        SimpleDateFormat sdf_out = new SimpleDateFormat(outputFormat);
	        
	        Date dt_obj = sdf_in.parse(date);
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dt_obj);
	        
	        newDate = sdf_out.format(cal.getTime());			
	    }catch(Exception e){
	    	Log.e("DateTime Converter","Date parsing failed: " + e);
	    }
	    
	    return newDate;
	}
	
	public static String durationPTHMFToTime(String duration){
		//Converts HTML5 Duration String (PT00H00M) to (HH:mm)

		String hours = duration.substring(2,4);
		String minutes = duration.substring(5,7);
		return hours + ":" + minutes;
	}
	
	/*
	 * Converts Time String 
	 */
	public static String timeToDurationPTHMFormat(String time){
		String[] separated = time.split(":");
		String hours = separated[0];
		String minutes = separated[1];
		
		if (hours.length() < 2){
			hours = "0" + hours;
		}
		
		if (minutes.length() < 2){
			minutes = "0" + minutes;
		}
		return "PT" + hours + "H" + minutes + "M";
	}
	
	/*
	 * Parses Excel Duration numeric Value 
	 * (1 = 24 hours)
	 */
	public static String parseExcerlDuration(double durationInDays){
	    String duration;
	    double durationInHours = durationInDays*24;
	    int hours = (int)Math.floor(durationInHours);
	    int minutes = (int)Math.floor(((double)(durationInHours-hours))*60);
	    String hrs_str = (String)((hours<10)?"0"+hours:String.valueOf(hours));
	    String min_str = "";
	    //add leading zero if needed
	    if (minutes<10){
	        min_str = "0"+minutes;
	    }else{
	        min_str = String.valueOf(minutes);
	    }
	    
	    duration = hrs_str + ":" + min_str;
	    return duration;
	}

	public static String getDate(long milliSeconds, String dateFormat)
	{
	    // Create a DateFormatter object for displaying date in specified format.
	    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
	    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    
	    // Create a calendar object that will convert the date and time value in milliseconds to date. 
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(milliSeconds);
	     
	     return formatter.format(calendar.getTime());
	}
	
	public static String dateMilisToString(long date){
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_DOTTED);
	    //sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	    String s = sdf.format(new Date(date));
		return s;
	}
	
	public static long dateStringToMilis(String date){
		//Parse String to Date
		Date dt;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_DOTTED);
		//sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			//Check the type of format (for example test split of '.', and split of '/'
			dt = sdf.parse(date);
			return dt.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String format(String date, String inFormat, String outFormat){
		String dateStr = "";

		try{
			SimpleDateFormat inFormatter = new SimpleDateFormat(inFormat);
			SimpleDateFormat outFormatter = new SimpleDateFormat(outFormat);	
			//inFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			//outFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date dt = inFormatter.parse(date);
			dateStr = outFormatter.format(dt);
		}catch(ParseException e){
			Log.e(LOG_TAG,"DateTime Parsing Error: " + e);
			Log.d(LOG_TAG,"Date String Input was: " + date);
		}catch(Exception e){
			Log.e(LOG_TAG,"Error: " + e);
		}
		return dateStr;
	}
	
	public static String getPrettyTime(String stringDate){
	    //get date Object
	    Date dt = new Date();
	    try {
	        dt = new SimpleDateFormat(DateTimeConverter.DATE_DEFAULT).parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
	    
	    Calendar date = Calendar.getInstance();
	    date.setTime(dt);
	    Calendar today = Calendar.getInstance();
	    
	    //Calculate Date Diffrence
	    
	    int years = today.get(Calendar.YEAR) - date.get(Calendar.YEAR);
	    int months = today.get(Calendar.MONTH) - date.get(Calendar.MONTH);
	    int days = today.get(Calendar.DAY_OF_MONTH) - date.get(Calendar.DAY_OF_MONTH);
	    
	    String prettyTime = "";
	    if (years != 0) {
	        prettyTime += years + " Years ";
	    }
	    if (months != 0){
	        prettyTime += months + " Months ";
	    }
	    if (days != 0){
	        prettyTime += days + " Days ";
	    }
	    
	    if (prettyTime == ""){
	        prettyTime = "Today";
	    }
	    
	    return "";
	}



}
