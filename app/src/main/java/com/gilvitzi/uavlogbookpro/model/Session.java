package com.gilvitzi.uavlogbookpro.model;

import android.content.ContentValues;
import android.content.Context;

import com.gilvitzi.uavlogbookpro.database.LogbookSQLite;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Session {
	private long id;
	private String date;
	private Duration duration;
	private String day_night;
	private String sim_actual;
	private String platform_type;
	private String platform_variation;
	private String registration;
	private String tail_number;
	private String icao;
	private String aerodrome_name;
	private String command;
	private String seat;
	private String flight_type;
	private String tags;
	private long takeoffs;
	private long landings;
	private long go_arounds;
	private String comments;
	
	public Session(long id,String date, Duration duration,
						 String day_night, String sim_actual,
						 String platform_type, String platform_variation,
						 String registration, String tail_number,
						 String icao, String aerodrome_name,
						 String command, String seat, String flight_type,
						 String tags,
						 long takeoffs, long landings, long go_arounds,
						 String comments){
		this.id = id;
		this.date = date;
		this.duration = duration;
		this.day_night = day_night;
		this.sim_actual = sim_actual;
		this.platform_type = platform_type;
		this.platform_variation = platform_variation;
		this.registration = registration;
		this.tail_number = tail_number;
		this.icao = icao;
		this.aerodrome_name = aerodrome_name;
		this.command = command;
		this.seat = seat;
		this.tags = tags;
		this.flight_type = flight_type;
		this.takeoffs = takeoffs;
		this.landings = landings;
		this.go_arounds = go_arounds;
		this.comments = comments;
		
	}
	
	public Session(){
		
	}
	
	//ID
	public long getId() {
	    return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	//DATE
	public String getDate() {
	    return this.date;
	}
	
	public String getDateString(Context context) {
	    //return DateTimeConverter.format(this.date, DateTimeConverter.ISO8601, DateTimeConverter.DATE_DEFAULT);
		Date prasedDate = DateTimeConverter.parseDate(this.date, DateTimeConverter.ISO8601);
		return DateTimeConverter.getFormattedDate(context, prasedDate);
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setDateFromString(String date){
		this.date = DateTimeConverter.format(date, DateTimeConverter.ISO8601, DateTimeConverter.ISO8601);
	}
	
	//DURATION ( ONLY IN MILLISECONDS)
	public Duration getDuration() {
	    return this.duration;
	}

	public String getDurationString() {
		return this.duration.getString();
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	//DAY NIGHT
	public String getDayNight() {
	    return this.day_night;
	}

	public void setDayNight(String day_night) {
		this.day_night = day_night;
	}
	
	//SIM ACTUAL
	public String getSimActual() {
	    return this.sim_actual;
	}

	public void setSimActual(String sim_actual) {
		this.sim_actual = sim_actual;
	}

	//PLATFORM_TYPE
	public String getPlatformType() {
	    return this.platform_type;
	}

	public void setPlatformType(String platform_type) {
		this.platform_type = platform_type;
	}

	//PLATFORM_TYPE
	public String getPlatformVariation() {
	    return this.platform_variation;
	}

	public void setPlatformVariation(String platform_variation) {
		this.platform_variation = platform_variation;
	}
	
	//Reg. and Tail NUM
	public String getRegistration() {
	    return this.registration;
	}
	
	public void setRegistration(String registration) {
		this.registration = registration;
	}
	
	public String getTailNumber() {
	    return this.tail_number;
	}
	
	public void setTailNumber(String tail_number) {
	    this.tail_number = tail_number;
	}
	
	//ICAO
	public String getICAO() {
	    return this.icao;
	}

	public void setICAO(String icao) {
		this.icao = icao;
	}
	
	//AERODROME NAME
	public String getAerodromeName() {
	    return this.aerodrome_name;
	}

	public void setAerodromeName(String aerodrome_name) {
		this.aerodrome_name = aerodrome_name;
	}
	
	//COMMAND
	public String getCommand() {
	    return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	//SEAT
	public String getSeat() {
	    return this.seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}
	
	//FLIGHT TYPE
	public String getFlightType() {
	    return this.flight_type;
	}

	public void setFlightType(String flight_type) {
		this.flight_type = flight_type;
	}
	
	//TAGS
	public String getTags() {
	    return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	//TAKEOFFS
	public long getTakeoffs() {
	    return this.takeoffs;
	}

	public void setTakeoffs(long takeoffs) {
		this.takeoffs = takeoffs;
	}
	
	//TAKEOFFS
	public long getLandings() {
	    return this.landings;
	}

	public void setLandings(long landings) {
		this.landings = landings;
	}
	
	//TAKEOFFS
	public long getGoArounds() {
	    return this.go_arounds;
	}

	public void setGoArounds(long go_arounds) {
		this.go_arounds = go_arounds;
	}
	
	//COMMENTS
	public String getComments() {
	    return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	public ContentValues getContentValues(){
		ContentValues contentValues = new ContentValues();
		
		contentValues.put(LogbookSQLite.COLUMN_DATE, String.valueOf(getDate()));
		contentValues.put(LogbookSQLite.COLUMN_DURATION, getDuration().getISO8601());
		contentValues.put(LogbookSQLite.COLUMN_DAY_NIGHT, getDayNight());
		contentValues.put(LogbookSQLite.COLUMN_SIM_ACTUAL, getSimActual());
		contentValues.put(LogbookSQLite.COLUMN_PLATFORM_TYPE, getPlatformType());
		contentValues.put(LogbookSQLite.COLUMN_PLATFORM_VARIATION, getPlatformVariation());
		contentValues.put(LogbookSQLite.COLUMN_REGISTRATION, getRegistration());
		contentValues.put(LogbookSQLite.COLUMN_TAIL_NUMBER, getTailNumber());
		contentValues.put(LogbookSQLite.COLUMN_ICAO, getICAO());
		contentValues.put(LogbookSQLite.COLUMN_AERODROME_NAME, getAerodromeName());
		contentValues.put(LogbookSQLite.COLUMN_COMMAND, getCommand());
		contentValues.put(LogbookSQLite.COLUMN_SEAT, getSeat());
		contentValues.put(LogbookSQLite.COLUMN_FLIGHT_TYPE, getFlightType());
		contentValues.put(LogbookSQLite.COLUMN_TAGS, getTags());
		contentValues.put(LogbookSQLite.COLUMN_TAKEOFFS, String.valueOf(getTakeoffs()));
		contentValues.put(LogbookSQLite.COLUMN_LANDINGS, String.valueOf(getLandings()));
		contentValues.put(LogbookSQLite.COLUMN_GO_AROUNDS, String.valueOf(getGoArounds()));
		contentValues.put(LogbookSQLite.COLUMN_COMMENTS, getComments());
		
		return contentValues;
	}
	
	public HashMap<String, String> getMap(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put(LogbookSQLite.COLUMN_ID,String.valueOf(getId()));
		map.put(LogbookSQLite.COLUMN_DATE,getDate());
		map.put(LogbookSQLite.COLUMN_DURATION,String.valueOf(getDuration()));
		map.put(LogbookSQLite.COLUMN_DAY_NIGHT,getDayNight());
		map.put(LogbookSQLite.COLUMN_SIM_ACTUAL,getSimActual());
		map.put(LogbookSQLite.COLUMN_PLATFORM_TYPE,getPlatformType());
		map.put(LogbookSQLite.COLUMN_PLATFORM_VARIATION,getPlatformVariation());
		map.put(LogbookSQLite.COLUMN_REGISTRATION,getRegistration());
		map.put(LogbookSQLite.COLUMN_TAIL_NUMBER,getTailNumber());
		map.put(LogbookSQLite.COLUMN_ICAO,getICAO());
		map.put(LogbookSQLite.COLUMN_AERODROME_NAME,getAerodromeName());
		map.put(LogbookSQLite.COLUMN_COMMAND,getCommand());
		map.put(LogbookSQLite.COLUMN_SEAT,getSeat());
		map.put(LogbookSQLite.COLUMN_FLIGHT_TYPE,getFlightType());
		map.put(LogbookSQLite.COLUMN_TAGS,getTags());
		map.put(LogbookSQLite.COLUMN_TAKEOFFS,String.valueOf(getTakeoffs()));
		map.put(LogbookSQLite.COLUMN_LANDINGS,String.valueOf(getLandings()));
		map.put(LogbookSQLite.COLUMN_GO_AROUNDS,String.valueOf(getGoArounds()));
		map.put(LogbookSQLite.COLUMN_COMMENTS,getComments());
		
		return map;
	}
	
	public ArrayList<NameValuePair> getList(){
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		HashMap<String, String> map = this.getMap();
		@SuppressWarnings("rawtypes")
		Iterator i = map.keySet().iterator();
		while(i.hasNext()) {
		    String key = (String)i.next();
		    String value = (String)map.get(key);
		    list.add(new NameValuePair(key,value));
		}
		return list;
	}

	public static Session parseFromList(List<String> row) {
		Session s = new Session();

		return s;
	}
}

