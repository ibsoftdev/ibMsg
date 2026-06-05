package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.exception.MsgException;
import com.ibsoft.ibMsg.util.mail.MailUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Service("MailChannelService")
public class ConfMailChannel extends ConfChannel {

   private static final Log logger = LogFactory.getLog(ConfMailChannel.class);
   private static final String PRODUCTION_ENV = "PR";

   private final MailUtil mailUtil = MailUtil.getInstance();

   @Value("${com.ibsoft.ibMsg.app:DE}")
   private String appEnv;

   @Override
   public void sendMessage(PushMessage pushMsg) throws MsgException {
      try (SmtpConnection smtp = SmtpConnection.open(this)) {
         for (AddresseePushMsg addr : pushMsg.getAddresseeMsgs()) {
            sendMail(pushMsg, addr, smtp);
         }
      } catch (MessagingException e) {
         throw new MsgException("Error SMTP: " + e.getMessage(), e);
      } catch (IOException e) {
         throw new MsgException("Error leyendo adjuntos: " + e.getMessage(), e);
      }
   }

   private void sendMail(PushMessage pushMsg, AddresseePushMsg addr, SmtpConnection smtp)
         throws MsgException, MessagingException, IOException {
      String from = addr.getAddresses_from();
      Recipients recipients = resolveRecipients(new Recipients(
            addr.getAddresses_to(),
            addr.getAddresses_cc(),
            addr.getAddresses_bcc()));

      logger.info("mail from=" + from + " to=" + recipients.to()
            + " cc=" + recipients.cc() + " bcc=" + recipients.bcc());

      InternetAddress fromAddress;
      InternetAddress[] toAddress;
      InternetAddress[] ccAddress;
      InternetAddress[] bccAddress;
      try {
         mailUtil.checkGoodEmail(from);
         fromAddress = new InternetAddress(from);
         toAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(recipients.to()));
         ccAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(recipients.cc()));
         bccAddress = toJakartaAddresses(MailUtil.getInternetAddressEmails(recipients.bcc()));
      } catch (javax.mail.internet.AddressException e) {
         throw new MsgException("Direccion de correo invalida: " + e.getMessage(), e);
      }

      if (toAddress == null && ccAddress == null && bccAddress == null) {
         throw new MsgException("Cannot send mail since all To, Cc, Bcc addresses are empty.");
      }

      MimeMessage msg = buildMimeMessage(smtp.session(), pushMsg, fromAddress,
            toAddress, ccAddress, bccAddress);
      smtp.send(msg);
   }

   /**
    * MECH_DEFAULT_TO valido tiene prioridad (sin cc/bcc).
    * addresses_to del mensaje solo si env=PR y no hay MECH_DEFAULT_TO valido.
    * Otro ambiente sin default_to: error.
    */
   private Recipients resolveRecipients(Recipients parsed) throws MsgException {
      if (hasValidDefaultTo()) {
         return new Recipients(getDefaultTo(), null, null);
      }
      if (PRODUCTION_ENV.equalsIgnoreCase(appEnv)) {
         return parsed;
      }
      throw new MsgException(
            "Se debe configurar MECH_DEFAULT_TO en MENT_CHANNEL para el ambiente de Desarrollo");
   }

   private MimeMessage buildMimeMessage(Session session, PushMessage pushMsg,
         InternetAddress from, InternetAddress[] to, InternetAddress[] cc, InternetAddress[] bcc)
         throws MessagingException, IOException {
      MimeMessage msg = new MimeMessage(session);
      msg.setSentDate(new Date());
      msg.setFrom(from);
      if (to != null) {
         msg.setRecipients(Message.RecipientType.TO, to);
      }
      if (cc != null) {
         msg.setRecipients(Message.RecipientType.CC, cc);
      }
      if (bcc != null) {
         msg.setRecipients(Message.RecipientType.BCC, bcc);
      }
      msg.setSubject(MimeUtility.encodeText(pushMsg.getSubject(), "iso-8859-1", "Q"));
      msg.setContent(buildContent(pushMsg));
      msg.saveChanges();
      return msg;
   }

   private MimeMultipart buildContent(PushMessage pushMsg) throws IOException, MessagingException {
      MimeMultipart multipart = new MimeMultipart();
      MimeBodyPart text = new MimeBodyPart();
      text.setContent(pushMsg.getBody(), "text/html; charset=utf-8");
      multipart.addBodyPart(text);

      if (pushMsg.getAttachmentMsgs() != null) {
         for (AttachmentPushMsg attachment : pushMsg.getAttachmentMsgs()) {
            String filePath = attachment.getFilePath();
            byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1);
            MimeBodyPart attach = new MimeBodyPart();
            attach.setDataHandler(new DataHandler(
                  new ByteArrayDataSource(fileContent, "application/octet-stream")));
            attach.setFileName(fileName);
            multipart.addBodyPart(attach);
         }
      }
      return multipart;
   }

   private record Recipients(String to, String cc, String bcc) {
   }

   /** Conexion SMTP reutilizable por envio (una por llamada a sendMessage). */
   private static final class SmtpConnection implements AutoCloseable {

      private final Session session;
      private final Transport transport;

      private SmtpConnection(Session session, Transport transport) {
         this.session = session;
         this.transport = transport;
      }

      static SmtpConnection open(ConfMailChannel channel) throws MessagingException {
         Properties props = channel.buildSmtpProperties();
         Session session = Session.getInstance(props, null);
         Transport transport = session.getTransport("smtp");
         channel.connectTransport(transport);
         return new SmtpConnection(session, transport);
      }

      Session session() {
         return session;
      }

      void send(MimeMessage message) throws MessagingException {
         transport.sendMessage(message, message.getAllRecipients());
      }

      @Override
      public void close() throws MessagingException {
         if (transport != null) {
            transport.close();
         }
      }
   }

   private Properties buildSmtpProperties() {
      Properties props = new Properties();
      props.put("mail.smtp.host", smtpHost());
      props.put("mail.smtp.port", smtpPort());
      String port = smtpPort();
      if ("587".equals(port) || "465".equals(port)) {
         props.put("mail.smtp.starttls.enable", "true");
      }
      if (smtpUser() != null) {
         props.put("mail.smtp.auth", "true");
      }
      return props;
   }

   private void connectTransport(Transport transport) throws MessagingException {
      String host = smtpHost();
      int port = Integer.parseInt(smtpPort());
      String user = smtpUser();
      if (user != null) {
         transport.connect(host, port, user, smtpPassword());
      } else {
         transport.connect(host, port, null, null);
      }
   }

   private String smtpHost() {
      String server = getUrlServer();
      int colon = server.lastIndexOf(':');
      return colon > 0 ? server.substring(0, colon) : server;
   }

   private String smtpPort() {
      String server = getUrlServer();
      int colon = server.lastIndexOf(':');
      return colon > 0 ? server.substring(colon + 1) : "25";
   }

   /** MECH_USER_SERVER: usuario o usuario||clave */
   private String smtpUser() {
      String userServer = getUserServer();
      if (userServer == null || userServer.isBlank()) {
         return null;
      }
      int sep = userServer.indexOf("||");
      return sep >= 0 ? userServer.substring(0, sep) : userServer;
   }

   private String smtpPassword() {
      String userServer = getUserServer();
      if (userServer == null || userServer.isBlank()) {
         return null;
      }
      int sep = userServer.indexOf("||");
      return sep >= 0 ? userServer.substring(sep + 2) : "";
   }

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
