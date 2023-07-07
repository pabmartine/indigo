package com.martinia.indigo.mail.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfiguration {

    private String host;
    private int port;
    private String username;
    private String password;
    private String encryption;
}
