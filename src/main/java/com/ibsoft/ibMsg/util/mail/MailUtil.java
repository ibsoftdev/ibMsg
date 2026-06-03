package com.ibsoft.ibMsg.util.mail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal replacement for legacy ibCommons MailUtil.
 * Only what ibMsg uses.
 */
public final class MailUtil {

   public static final int MAX_MESSAGES_PER_TRANSPORT = 100;

   private static final MailUtil INSTANCE = new MailUtil();

   private MailUtil() {
   }

   public static MailUtil getInstance() {
      return INSTANCE;
   }

   public void checkGoodEmail(String email) throws AddressException {
      if (email == null || email.isBlank()) {
         throw new AddressException("Email is empty");
      }
      new InternetAddress(email, true);
   }

   public static InternetAddress[] getInternetAddressEmails(String emails) throws AddressException {
      if (emails == null) return null;
      String trimmed = emails.trim();
      if (trimmed.isEmpty()) return null;

      // Accept common separators: ';' ',' whitespace
      String normalized = trimmed.replace('|', ';');
      String[] parts = normalized.split("[,;\\s]+");

      List<InternetAddress> result = new ArrayList<>();
      for (String p : parts) {
         String e = p.trim();
         if (e.isEmpty()) continue;
         result.add(new InternetAddress(e, true));
      }
      return result.isEmpty() ? null : result.toArray(new InternetAddress[0]);
   }
}

