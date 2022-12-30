package com.martinia.indigo.domain.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@Slf4j
public class DataUtils {

    @SneakyThrows
    public static String getData(String _url) {
        URL url = new URL(_url);
        String data = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection()
                .getInputStream()))) {
            String line = null;
            while (null != (line = br.readLine())) {
                data += line.trim();
            }
        }
        return StringUtils.isEmpty(data) ? null : data;
    }

}
