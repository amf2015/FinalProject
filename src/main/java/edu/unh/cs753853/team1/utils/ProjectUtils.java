package edu.unh.cs753853.team1.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProjectUtils {
	public static final String df = "yyyy-MM-dd";
	public static final String df2 = "yyyy-MM-dd HH:mm";
	public static final String df3 = "yyyyMMdd-HHmm";
	// public static final String df3 = "MMMMM dd, EEE hh:mm:ss aaa";
	public static Gson gson;

	// Utils functions
	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = ProjectConfig.OUTPUT_DIRECTORY + "/" + filename;
		try (FileWriter runfile = new FileWriter(new File(fullpath))) {
			for (String line : runfileStrings) {
				runfile.write(line + "\n");
			}

			runfile.close();
		} catch (IOException e) {
			System.out.println("Could not open " + fullpath);
		}
	}

	public static String getTimeStr(long secs) {
		int hours = secs >= 3600 ? (int) (secs / 3600) : 0;
		int mins = secs >= 60 ? ((int) ((secs / 60) % 60)) : 0;
		int sec = (int) (secs % 60);
		return (hours > 0 ? hours + " hours " : "") + ((hours > 0 || mins > 0) ? mins + " mins " : "")
				+ (hours > 0 ? "" : sec + " secs");
	}

	public static Date getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST"));
		return cal.getTime();
	}

	public static Date getDate(String dateStr) {
		try {
			return new SimpleDateFormat(df2).parse(dateStr);
		} catch (ParseException e) {
			try {
				return new SimpleDateFormat(df3).parse(dateStr);
			} catch (ParseException e1) {
				return null;
			}
		}
	}

	public static Date getQueryDate(String dateStr) {
		try {
			return new SimpleDateFormat(df).parse(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String getDateStr(Date date) {
		return getDateStr(date, df);
	}

	public static String getDateStr(Date date, String dataFromat) {
		return new SimpleDateFormat(dataFromat).format(date);
	}

	public static Gson getGsonStringBuilder() {
		if (gson == null) {
			// Create gson to exclude Non-expose fields in entity class.
			gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		}
		return gson;
	}
}
