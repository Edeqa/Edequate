package com.edeqa.edequate.helpers;

import com.edeqa.helpers.Misc;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {
    public int sendMail(String smtpServerHost, String smtpServerPort, String smtpUserName, String smtpUserPassword, String smtpUserAccessToken, String fromUserEmail, String fromUserFullName, String toEmail, String subject, String body) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", smtpServerPort);
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromUserEmail, fromUserFullName));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(body, "text/plain");
        msg.saveChanges();

        SMTPTransport transport = new SMTPTransport(session, null);
        if(smtpUserPassword != null) {
            transport.connect(smtpServerHost, smtpUserName, smtpUserPassword);
        } else if(smtpUserAccessToken != null) {
            transport.connect(smtpServerHost, smtpUserName, null);
            transport.issueCommand("AUTH XOAUTH2 " + new String(BASE64EncoderStream.encode(String.format("user=%s\1auth=Bearer %s\1\1", smtpUserName, smtpUserAccessToken).getBytes())), 235);
        } else {
            Misc.err("Settings", "Password or token not defined");
        }
        transport.sendMessage(msg, msg.getAllRecipients());
        return transport.getLastReturnCode();
    }
}