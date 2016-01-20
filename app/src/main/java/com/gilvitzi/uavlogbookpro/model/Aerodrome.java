package com.gilvitzi.uavlogbookpro.model;


public class Aerodrome {
    final String LOGTAG = "Aerodrome Class";
    
    private long id = 0;
    private String icao = "";
    private String aerodromeName = "";

    
    public Aerodrome(long id,String icao,String name){
        this.id = id;
        this.icao = icao;
        this.aerodromeName = name;
    }
            
    public long getID(){
        return this.id;
    }
    
    public String getICAO(){
        return this.icao;
    }
    
    public String getAerodromeName(){
        return this.aerodromeName; 
    }
    
    public String toString(){
        return icao + "  " + aerodromeName;
    }
}
