package com.example.javamail.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JavamailServiceTest {
    private static GreenMail smtpServer = new GreenMail(new ServerSetup(2525, "localhost", "smtp"));

    @Autowired
    JavamailService javamailService;

    @BeforeAll
    static void setUp() {
        System.out.println("Starting server");
        smtpServer.start();
    }

    @AfterAll
    static void tearDown() {
        System.out.println("Stopping server");
        smtpServer.stop();
    }

    @Test
    void testSend() throws MessagingException {
        MimeMessage[] messages = smtpServer.getReceivedMessages();
        assertEquals(0,messages.length);
        javamailService.send();
        messages = smtpServer.getReceivedMessages();
        assertEquals(1,messages.length);
        assertEquals("me@example.com", messages[0].getFrom()[0].toString());
    }
}
