package com.martinia.indigo.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@Slf4j
@Component
public class DataUtils {

    @SneakyThrows
    public String getData(String _url) {
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
