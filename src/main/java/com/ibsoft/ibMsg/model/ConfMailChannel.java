package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.exception.MsgException;
import com.ibsoft.ibMsg.util.mail.MailUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

@Service("MailChannelService")
public class ConfMailChannel extends ConfChannel {

   private static Log logger = LogFactory.getLog(ConfMailChannel.class);

   protected MailUtil mus = MailUtil.getInstance();

   @Value("${com.ibsoft.ibMsgEnv.app_env:DE}")
   private String appEnv;

   @Override
   public void sendMessage(PushMessage pushMsg) throws MsgException {
      for (AddresseePushMsg addr: pushMsg.getAddresseeMsgs()){
         sendMail(pushMsg, addr);
      }

   }

   public void sendMail(PushMessage pushMsg, AddresseePushMsg addr) throws MsgException {
      Session session = null;
      Transport transport = null;
      //int totalEmails = mailStructCollection.size();
      int count = 0;
      int sendFailedExceptionCount = 0;
      try {
         count++;

         if ((transport == null) || (session == null)) {
            Properties props = new Properties();
            String server = getUrlServer();
            String host = server.substring(0,getUrlServer().lastIndexOf(":"));
            String port = server.substring(getUrlServer().lastIndexOf(":")+1);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            session = Session.getDefaultInstance(props, null);
            transport = session.getTransport("smtp");
            //todo: Deshabilitado para pruebas
            /*if ((getUserServer() != null) && (getUserServer().length() > 0)) {
               props.put("mail.smtp.auth", "true");
               props.put("mail.smtp.starttls.enable", "true");
               transport.connect(server, getUserServer(), ""*//*getPassword()*//*);
            }
            else {
               transport.connect();
            }*/
         }

         String from = addr.getAddresses_from();
         String to = addr.getAddresses_to();
         String defaultto = getDefaultTo();
         String cc = "";
         String bcc = "";
         String subject = pushMsg.getSubject();
         String message = pushMsg.getBody();

         //actualmente las direcciones vienen con el formato: to:mail1;mail2|cc:mailcc1;mailcc2|bcc:mailbcc1;mailbcc2
         logger.info("mails original:"+to);
         StringTokenizer tokenizer = new StringTokenizer(to, "\\|");
         while (tokenizer.hasMoreTokens()){
            String mails = tokenizer.nextToken();
            if (mails.contains("to:")){
               to = mails.substring(3);
            }
            else if (mails.contains("cc:")){
               cc = mails.substring(3);
            }
            else if (mails.contains("bcc:")){
               bcc = mails.substring(4);
            }
         }
         logger.info("mails despues. to:"+to+" cc:"+cc+" bcc:"+bcc);

         //esto obliga a que las direcciones de envio sean las que estan en la tabla MENT_CHANEL, cuando tiene valor
         if (defaultto != null && !defaultto.equals("")) {
            to = defaultto;
            cc = null;
            bcc = null;
         }
         else if (!"PR".equalsIgnoreCase(appEnv))
            throw new MsgException("Se debe configurar un 'default_to' para el ambiente de Desarrollo");

         try {
            // this will also check for email error
            mus.checkGoodEmail(from);
            InternetAddress fromAddress = new InternetAddress(from);
            InternetAddress[] toAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(to));
            InternetAddress[] ccAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(cc));
            InternetAddress[] bccAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(bcc));
            if ((toAddress == null) && (ccAddress == null) &&
                  (bccAddress == null)) {
               throw new MsgException("Cannot send mail since all To," +
                     " Cc, Bcc addresses are empty.");
            }

            // create a message
            Message msg = new MimeMessage(session);
            msg.setSentDate(new Date());
            msg.setFrom(fromAddress);

            if (toAddress != null) {
               msg.setRecipients(Message.RecipientType.TO, toAddress);
            }
            if (ccAddress != null) {
               msg.setRecipients(Message.RecipientType.CC, ccAddress);
            }
            if (bccAddress != null) {
               msg.setRecipients(Message.RecipientType.BCC, bccAddress);
            }
            //This code is use to display unicode in Subject
            msg.setSubject(MimeUtility.encodeText(subject,
                  "iso-8859-1", "Q"));

            // Cuando se tiene archivos adjuntos a enviar, se tiene utilizar MimeMultipart que tenga el mensaje y el archivo,
            // caso contrario con setText se coloca el mensaje.
            // Nota: para soportar archivos pdf se debe utilizar javamail 1.4
            if (pushMsg.getAttachmentMsgs() == null || pushMsg.getAttachmentMsgs().size() == 0){
               MimeMultipart mp = new MimeMultipart();
               //Texto
               BodyPart text = new MimeBodyPart();
               text.setContent( message, "text/html; charset=utf-8" );
               mp.addBodyPart(text);
               msg.setContent(mp);
            }
            else {
               MimeMultipart mp = new MimeMultipart();
               //Texto
               BodyPart text = new MimeBodyPart();
               text.setContent( message, "text/html; charset=utf-8" );
               //text.setText(message);
               mp.addBodyPart(text);
               // Attachments
               for (AttachmentPushMsg attachmentPushMsg : pushMsg.getAttachmentMsgs()) {
                  String filePath = attachmentPushMsg.getFilePath();
                  byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
                  String extension = filePath.substring(filePath.lastIndexOf(".")+1);
                  String fileName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
                  BodyPart atach = new MimeBodyPart();
                  atach.setDataHandler(new DataHandler(new ByteArrayDataSource(fileContent, "application/octet-stream")));
                  atach.setFileName(fileName);
                  mp.addBodyPart(atach);
               }

               msg.setContent(mp);
            }

            msg.saveChanges();

            //todo: Deshabilitado para pruebas
            //transport.sendMessage(msg, msg.getAllRecipients());

            // now check if sent 100 emails, then close connection (transport)
            if ((count % MailUtil.MAX_MESSAGES_PER_TRANSPORT) == 0) {
               try {
                  if (transport != null) transport.close();
               }
               catch (MessagingException ex) {
                  throw new MsgException(ex.getMessage());
               }
               transport = null;
               session = null;
            }
         }
         catch (SendFailedException ex) {
            sendFailedExceptionCount++;
            logger.error("SendFailedException has occured.", ex);
            logger.warn("SendFailedException has occured. Detail info:");
            logger.warn("from = " + from);
            logger.warn("to = " + to);
            logger.warn("cc = " + cc);
            logger.warn("bcc = " + bcc);
            //logger.warn("subject = " + subject);
            //logger.info("message = " + message);
            /*if ((totalEmails != 1) && (sendFailedExceptionCount > 10)) {
               throw ex;// this may look redundant, but it is not :-)
            }
            else if (totalEmails == 1){
               throw ex;// this may look redundant, but it is not :-)
            }*/
            throw new MsgException(ex.getMessage());
         }
         catch (MessagingException mex) {
            logger.error("MessagingException has occured.", mex);
            logger.warn("MessagingException has occured. Detail info:");
            logger.warn("from = " + from);
            logger.warn("to = " + to);
            logger.warn("cc = " + cc);
            logger.warn("bcc = " + bcc);
            logger.warn("subject = " + subject);
            logger.info("message = " + message);
            //throw mex;// this may look redundant, but it is not :-)
            throw new MsgException(mex.getMessage());
         } catch (IOException e) {
            logger.error("IOException has occured.", e);
            throw new MsgException(e.getMessage());
         }

      }
      catch (Exception e){
         throw new MsgException(e.getMessage());
      }
      finally {
         try {
            if (transport != null) transport.close();
         }
         catch (MessagingException ex) {
            logger.error("MessagingException has occured.", ex);
            throw new MsgException(ex.getMessage());
         }
         /*if (totalEmails != 1) {
            logger.info("sendMail: totalEmails = " + totalEmails + " sent count = "
                  + count);
         }*/
      }
   }

   /** Puente ibCommons (javax.mail) → Jakarta Mail en Spring Boot 3. */
   private static InternetAddress[] toJakartaAddresses(javax.mail.internet.InternetAddress[] legacy)
         throws AddressException {
      if (legacy == null) {
         return null;
      }
      InternetAddress[] result = new InternetAddress[legacy.length];
      for (int i = 0; i < legacy.length; i++) {
         result[i] = new InternetAddress(legacy[i].getAddress());
      }
      return result;
   }
}
