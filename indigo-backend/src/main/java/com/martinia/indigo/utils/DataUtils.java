package com.martinia.indigo.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.LoggerFactory;

public class DataUtils {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DataUtils.class);

	public static String getData(String _url) {
		String ret = null;
		try {
			URL url = new URL(_url);
			String data = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
				String line = null;
				while (null != (line = br.readLine())) {
					line = line.trim();
					if (true) {
						data += line;
					}
				}
			}
			ret = data;
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return ret;
	}

}
