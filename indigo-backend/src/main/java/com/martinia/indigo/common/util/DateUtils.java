package com.martinia.indigo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class DateUtils {

	public static Date parseDate(String dateStr) {
		final String[] possibleFormats = { "yyyy-MM-dd", "yyyy", "yyyy-MM", "yyyy-MM-dd'T'HH:mm:ssXXX"  };

		for (String format : possibleFormats) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				return sdf.parse(dateStr);
			}
			catch (ParseException e) {
				if (dateStr.length()>7)
					log.error(e.getMessage());
			}
		}
		return null;
	}

}
