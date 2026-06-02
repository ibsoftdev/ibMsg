package com.ibsoft.ibMsg.model;

public class ConfMessage {

   private String subject;
   private String body;
   private ConfChannel channel;
   private Integer nu_campana;
   private Integer nu_mensaje;
   private Integer time_next;

   public ConfMessage() {
   }

   public ConfMessage(String subject, String body, ConfChannel channel) {
      this.subject = subject;
      this.body = body;
      this.channel = channel;
   }

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getBody() {
      return body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public ConfChannel getChannel() {
      return channel;
   }

   public void setChannel(ConfChannel channel) {
      this.channel = channel;
   }

   public Integer getNu_campana() {
      return nu_campana;
   }

   public void setNu_campana(Integer nu_campana) {
      this.nu_campana = nu_campana;
   }

   public Integer getNu_mensaje() {
      return nu_mensaje;
   }

   public void setNu_mensaje(Integer nu_mensaje) {
      this.nu_mensaje = nu_mensaje;
   }

   public Integer getTime_next() { return time_next; }

   public void setTime_next(Integer time_next) { this.time_next = time_next; }
}