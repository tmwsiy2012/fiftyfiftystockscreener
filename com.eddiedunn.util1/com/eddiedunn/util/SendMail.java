package com.eddiedunn.util;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {

	public static void sendEmail( String smtpServer, String fromAddress, String toAddresses, String subject, String body, String csv, String marketName){
		try {

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", smtpServer);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set the RFC 822 "From" header field using the 
        // value of the InternetAddress.getLocalAddress method.
        message.setFrom(new InternetAddress(fromAddress));
        String[] addys = toAddresses.split(";");
        for (int i = 0; i < addys.length; i++) {
            // Add the given addresses to the specified recipient type.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(addys[i]));
		}


        // Set the "Subject" header field.
        message.setSubject(subject);
  
        // Create the message part 
        BodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        messageBodyPart.setContent(body,"text/html");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        String filename = DateUtils.now("yyyy.MM.dd")+" "+marketName+".csv";
        messageBodyPart.setFileName(filename);
        messageBodyPart.setText(csv);
        multipart.addBodyPart(messageBodyPart);

        // Put parts in message
        message.setContent(multipart);

        // Send message
        Transport.send(message);

        System.out.println("Message Sent");
		
		} catch (Exception e) {
			System.err.println("Error sending Email");
			e.printStackTrace();
		}     
	}
}
