package com.gilvitzi.uavlogbookpro.database;

import com.gilvitzi.uavlogbookpro.util.StringValuePair;

import java.util.Calendar;

/**
 * Created by Gil on 28/01/2016.
 */
public class LogbookReportQuery {

    public static String getYearlyCAAReport(int year) {
        String query =  "SELECT " +
                "strftime('%d.%m.%Y',date) AS 'Date'," +
                LogbookSQLite.DURATION_HOURS + " AS 'Hours', "+
                "(platform_type || ' ' || platform_variation) AS 'Platform',"+
                "icao AS 'Location',"+
                "registration AS 'Reg.',"+
                "tail_number AS 'Tail No.',"+
                "(CASE WHEN "+
                "((command=='PIC' OR command=='Instructor') AND sim_actual<>'Simulator') "+
                "THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") "+
                "ELSE 0 "+
                "END) "+
                " AS 'PIC',"+
                "(CASE command WHEN 'SIC' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'SIC',"+
                "(CASE command WHEN 'Instructor' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Instructor',"+
                "(CASE command WHEN 'Trainee' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Trainee',"+
                "(CASE sim_actual WHEN 'Simulator' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Sim',"+
                "day_night AS 'Day / Night' "+
                " FROM logbook " +
                " WHERE strftime('%Y', date) = '" + year + "'";

        return query;
    }

    public static String getCaaReport() {
        String query = "SELECT " +
                "strftime('%d.%m.%Y',date) AS 'Date'," +
                LogbookSQLite.DURATION_HOURS + " AS 'Hours', "+
                "(platform_type || ' ' || platform_variation) AS 'Platform',"+
                "icao AS 'Location',"+
                "registration AS 'Reg.',"+
                "tail_number AS 'Tail No.',"+
                "(CASE WHEN "+
                "((command=='PIC' OR command=='Instructor') AND sim_actual<>'Simulator') "+
                "THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") "+
                "ELSE 0 "+
                "END) "+
                " AS 'PIC',"+
                "(CASE command WHEN 'SIC' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'SIC',"+
                "(CASE command WHEN 'Instructor' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Instructor',"+
                "(CASE command WHEN 'Trainee' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Trainee',"+
                "(CASE sim_actual WHEN 'Simulator' THEN (" + LogbookSQLite.DURATION_HOURS_DECIMAL + ") ELSE 0 END) AS 'Sim',"+
                "day_night AS 'Day / Night' "+
                " FROM logbook";

        return query;
    }

    public static String getHoursPerYear() {
        String query = "SELECT " +
                "strftime('%Y',date) AS 'Year'," +
                LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+
                "COUNT(*) AS 'Sessions'" +
                "FROM logbook " +
                "GROUP BY strftime('%Y',date) " +
                "ORDER BY strftime('%Y',date) DESC";
        return query;
    }

    public static String getHoursPerLocation() {
        String query = "SELECT " +
                "( aerodrome_name || ' (' || icao || ')') AS 'Aerodrome'," +
                LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+
                "COUNT(*) AS 'Sessions'" +
                "FROM logbook " +
                "GROUP BY icao,aerodrome_name " +
                "ORDER BY SUM(duration) DESC";

        return query;
    }

    public static String getHoursPerPlatform() {
        String query = "SELECT " +
                "(platform_type || ' ' || platform_variation) AS 'Platform', " +

                LogbookSQLite.DURATION_SUM_HOURS + " AS 'Hours', "+

                "COUNT(*) AS 'Sessions'" +
                "FROM logbook " +
                "GROUP BY platform_type,platform_variation " +
                "ORDER BY SUM(duration) DESC";

        return query;
    }

    public static String getSessionDatePerPlatform() {
        String query = "SELECT " +
                "(platform_type || ' ' || platform_variation) AS 'Platform'," +
                "strftime('%d.%m.%Y',MAX(date)) AS 'Last Session Date' " +
                "FROM logbook " +
                "GROUP BY platform_type,platform_variation " +
                "ORDER BY MAX(date) DESC";

        return query;
    }

    public static String getSessionsCountedActivities() {
        String query = "SELECT platform_type AS 'Platform Type'," +
                "SUM(takeoffs) AS 'Takeoffs'," +
                "SUM(landings) AS 'Landings'," +
                "SUM(go_arounds) AS 'Go Arounds' " +
                "FROM logbook GROUP BY platform_type";

        return query;
    }

    public static String getSessionsPerPLatform(StringValuePair platformTypeAndVariation) {
        String platformType = platformTypeAndVariation.getFirst();
        String platformVariation = platformTypeAndVariation.getSecond();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * ");
        query.append(" FROM ").append(LogbookSQLite.TABLE_LOGBOOK);
        query.append(" WHERE " + LogbookSQLite.COLUMN_PLATFORM_TYPE + "= '" + platformType + "'");
        query.append(" AND ");
        query.append(LogbookSQLite.COLUMN_PLATFORM_VARIATION + "= '" + platformVariation + "'");
        query.append(String.format(" ORDER BY %1$s DESC",LogbookSQLite.COLUMN_DATE));
        return query.toString();
    }

    public static String getSessionsThisYear() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        String time = year + "-01-01 00:00:00";
        String query = LogbookSQLite.SELECT_ALL_SESSIONS +
                " WHERE " + LogbookSQLite.COLUMN_DATE + ">= '" + time + "'";

        return query;
    }

    public static String getAllSessions() {
        String query = LogbookSQLite.SELECT_ALL_SESSIONS +
                " ORDER BY date DESC";

        return query;
    }

    public static String getSessionsByTag(String tag) {
        String query = LogbookSQLite.SELECT_ALL_SESSIONS +
                " WHERE tags LIKE '%" + tag + "%'" +
                " ORDER BY date DESC";

        return query;
    }
}