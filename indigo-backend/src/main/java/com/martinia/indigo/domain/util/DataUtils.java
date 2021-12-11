package com.martinia.indigo.domain.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataUtils {

  public static String getData(String _url) {
    String ret = null;
    try {
      URL url = new URL(_url);
      String data = "";
      try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection()
          .getInputStream()))) {
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
      ;
    }
    return ret;
  }

}
