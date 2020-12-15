/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package org.cricketmsf.microsite.out.notification;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class SmtpSender2 extends OutboundAdapter implements EmailSenderIface, Adapter {
    private static final Logger logger = LoggerFactory.getLogger(SmtpSender2.class);
    String from = "";
    String forcedCC = null;
    String forcedBCC = null;
    String mailhost = "";
    String localhost = "";
    String mailer = "";
    String protocol = "SMTP";
    String startTls = "false";
    String user = "";
    String password = "";
    String debugSession = null;
    boolean ready = true;
    boolean usingTls = false;
    Integer port = null;

    protected HashMap<String, String> statusMap = null;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {

        from = properties.getOrDefault("from", "");
        forcedCC = properties.getOrDefault("cc", "");
        forcedBCC = properties.getOrDefault("bcc", "");
        mailhost = properties.getOrDefault("mailhost", "");
        localhost = properties.getOrDefault("localhosthost", "");
        mailer = properties.getOrDefault("mailer", "signomix");
        protocol = properties.getOrDefault("protocol", "SMTP");
        user = properties.getOrDefault("user", "");
        password = properties.getOrDefault("password", "");
        debugSession = properties.getOrDefault("debug-session", "false");
        startTls = properties.getOrDefault("starttls", "false");
        usingTls = Boolean.getBoolean(startTls);
        try {
            port = Integer.parseInt(properties.get("port"));
        } catch (Exception e) {
        }
        if (from.isEmpty() || mailhost.isEmpty() || user.isEmpty() || password.isEmpty()) {
            ready = false;
        }
        logger.info("\tfrom: " + from);
        logger.info("\tcc: " + forcedCC);
        logger.info("\tbcc: " + forcedBCC);
        logger.info("\tmailhost: " + mailhost);
        logger.info("\tlocalhost: " + localhost);
        logger.info("\tprotocol: " + protocol);
        logger.info("\tmailer: " + mailer);
        logger.info("\tstarttls: " + startTls);
        logger.info("\tport: " + port);
        logger.info("\tuser: " + user);
        logger.info("\tpassword: " + (password.isEmpty() ? "" : "******"));
        logger.info("\tdebug-session: " + debugSession);
    }

    @Override
    public String send(String recipient, String topic, String content) {
        return send(recipient,"","",topic,content);
    }

    @Override
    public void updateStatusItem(String key, String value) {
        statusMap.put(key, value);
    }

    @Override
    public String send(String to, String cc, String bcc, String subject, String text) {
        if ((null == to || to.isBlank())&&(null == cc || cc.isBlank())&&(null == bcc || bcc.isBlank())) {
            return "";
        }
        String finalCC="";
        if(!forcedCC.isBlank()&&!cc.isBlank()){
            finalCC=forcedCC+";"+cc;
        }else if(!forcedCC.isBlank()){
            finalCC=forcedCC;
        }else{
            finalCC=cc;
        }
        String finalBCC="";
        if(!forcedBCC.isBlank()&&!bcc.isBlank()){
            finalBCC=forcedBCC+";"+bcc;
        }else if(!forcedBCC.isBlank()){
            finalBCC=forcedBCC;
        }else{
            finalBCC=bcc;
        }
        String result = "OK";
        Properties props = System.getProperties();
        final String USERNAME = from;
        boolean debug = Boolean.parseBoolean(debugSession);

        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.host", mailhost);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", password);
        if (usingTls) {
            if (null == port) {
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.socketFactory.port", "587");
            } else {
                props.put("mail.smtp.port", "" + port);
                props.put("mail.smtp.socketFactory.port", "" + port);
            }
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtps.auth", "true");
        } else {
            if (null != port) {
                props.put("mail.smtp.port", "" + port);
            }
            props.put("mail.smtp.auth", "true");
        }
        props.put("mail.smtp.socketFactory.fallback", "false");
        Session session = Session.getDefaultInstance(props);
        if (debug) {
            session.setDebug(true);
        }
        try {

            MimeMessage mime = new MimeMessage(session);
            mime.setFrom(new InternetAddress(USERNAME));
            mime.setSubject(subject);
            //mime.setText(content);
            mime.setContent(text, "text/html");
            mime.setHeader("X-Mailer", mailer);
            mime.setHeader("XPriority", "1");
            mime.setSentDate(new Date());
            if (null != to) {
                mime.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
            if (cc != null) {
                mime.setRecipients(Message.RecipientType.CC, finalCC);
            }
            if (bcc != null) {
                mime.setRecipients(Message.RecipientType.BCC, finalBCC);
            }

            Transport transport = session.getTransport("smtps");
            transport.connect(mailhost, from, password);
            transport.sendMessage(mime, mime.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.warn("smtp user=" + user);
            logger.warn(e.getMessage());
            result = "ERROR: " + e.getMessage();
        }
        return result;
    }

    @Override
    public String send(String[] recipients, String[] names, String subject, String text) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
