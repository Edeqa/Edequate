package com.edeqa.edequate.helpers;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.rest.system.Arguments;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;
import static com.edeqa.edequate.rest.admin.Settings.MAIL;
import static com.edeqa.edequate.rest.admin.Settings.REPLY_EMAIL;
import static com.edeqa.edequate.rest.admin.Settings.REPLY_NAME;
import static com.edeqa.edequate.rest.admin.Settings.SMTP_LOGIN;
import static com.edeqa.edequate.rest.admin.Settings.SMTP_PASSWORD;
import static com.edeqa.edequate.rest.admin.Settings.SMTP_PORT;
import static com.edeqa.edequate.rest.admin.Settings.SMTP_SERVER;

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
        props.put("mail.from", getFromEmail());

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);

        msg.setReplyTo(new javax.mail.Address[]{new javax.mail.internet.InternetAddress(getFromEmail())});
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

    public SendMail useMailer() throws IOException {
        Arguments arguments = (Arguments) ((EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS)).getHolder(Arguments.TYPE);

        WebPath settingsWebPath = new WebPath(arguments.getWebRootDirectory(), "data/.settings.json");
        JSONObject settingsJSON = new JSONObject(settingsWebPath.content());
        JSONObject smtpJSON = settingsJSON.getJSONObject(MAIL);

        setServerHost(smtpJSON.getString(SMTP_SERVER));
        setServerPort(smtpJSON.getString(SMTP_PORT));
        setLogin(smtpJSON.getString(SMTP_LOGIN));
        setPassword(smtpJSON.getString(SMTP_PASSWORD));
        setFromEmail(smtpJSON.getString(REPLY_EMAIL));
        setFromUsername(smtpJSON.getString(REPLY_NAME));

        return this;
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