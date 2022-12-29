package com.martinia.indigo.adapters.in.rest.controllers;

import com.martinia.indigo.domain.services.MailServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/util")
public class UtilRestController {

    @Autowired
    private MailServiceImpl mailService;

    @GetMapping(value = "/testmail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> testmail(@RequestParam String address) {
        mailService.testEmail(address);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/mail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> mail(@RequestParam String path, @RequestParam String address) throws Exception {

        String error = mailService.mail(path, address);


        if (StringUtils.isEmpty(error)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            throw new Exception(error);
        }
    }

}
