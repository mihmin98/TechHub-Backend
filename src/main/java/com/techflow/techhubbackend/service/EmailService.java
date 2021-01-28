package com.techflow.techhubbackend.service;

import org.springframework.stereotype.Service;
 
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Properties;

@Service("emailService")
public class EmailService 
{
  
    public void sendMail(String to, String code) throws AddressException, MessagingException 
    {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        Session session = Session.getInstance(prop,
        new javax.mail.Authenticator() {
           protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("no.reply.techhub@gmail.com", "1234parola1234");
      }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("no.reply.techhub@gmail.com"));
        message.setRecipients(
        Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Discount code: ");

        String msg = "Your discount code is: " + code;

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

}