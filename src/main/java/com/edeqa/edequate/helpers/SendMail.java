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
    private String serverHost;
    private String serverPort;
    private String login;
    private String password;
    private String oauth2Token;
    private String fromUsername;
    private String fromEmail;
    private String toEmail;
    private String toUsername;
    private String subject;
    private String body;


    public int send() throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", getServerPort());
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(getFromEmail(), getFromUsername()));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(getToEmail()));
        msg.setSubject(getSubject());
        msg.setSentDate(new Date());
        msg.setContent(getBody(), "text/plain");
        msg.saveChanges();

        SMTPTransport transport = new SMTPTransport(session, null);
        if(getPassword() != null) {
            transport.connect(getServerHost(), getLogin(), getPassword());
        } else if(getOauth2Token() != null) {
            transport.connect(getServerHost(), getLogin(), null);
            transport.issueCommand("AUTH XOAUTH2 " + new String(BASE64EncoderStream.encode(String.format("user=%s\1auth=Bearer %s\1\1", getLogin(), getOauth2Token()).getBytes())), 235);
        } else {
            Misc.err("Settings", "Password or token not defined");
        }
        transport.sendMessage(msg, msg.getAllRecipients());
        return transport.getLastReturnCode();
    }

    public String getServerHost() {
        return serverHost;
    }

    public SendMail setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public String getServerPort() {
        return serverPort;
    }

    public SendMail setServerPort(String serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public SendMail setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SendMail setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getOauth2Token() {
        return oauth2Token;
    }

    public SendMail setOauth2Token(String oauth2Token) {
        this.oauth2Token = oauth2Token;
        return this;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public SendMail setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
        return this;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public SendMail setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
        return this;
    }

    public String getToEmail() {
        return toEmail;
    }

    public SendMail setToEmail(String toEmail) {
        this.toEmail = toEmail;
        return this;
    }

    public String getToUsername() {
        return toUsername;
    }

    public SendMail setToUsername(String toUsername) {
        this.toUsername = toUsername;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public SendMail setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getBody() {
        return body;
    }

    public SendMail setBody(String body) {
        this.body = body;
        return this;
    }
}