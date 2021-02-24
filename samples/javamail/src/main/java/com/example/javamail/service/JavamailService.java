package com.example.javamail.service;

import com.sun.mail.util.MailConnectException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;

@RestController
public class JavamailService {

    private JavaMailSender javaMailSender;

    public JavamailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @GetMapping("/")
    public String send() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("you@example.com");
            message.setFrom("me@example.com");
            message.setSubject("My very email");
            message.setText("Very nice content!");
            javaMailSender.send(message);
        } catch (MailException e) {
            // This is ok as we don't have any server available
        }
        return "OK";
    }
}
