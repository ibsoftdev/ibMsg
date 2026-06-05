package com.ibsoft.ibMsg.model;

public class AddresseePushMsg {

   private Long   id_msg;
   private String addresses_to;
   private String addresses_cc;
   private String addresses_bcc;
   private String addresses_from;

   public AddresseePushMsg() {
   }

   public AddresseePushMsg(Long id_msg, String addresses_to, String addresses_from) {
      this.id_msg = id_msg;
      this.addresses_to = addresses_to;
      this.addresses_from = addresses_from;
   }

   public Long getId_msg() {
      return id_msg;
   }

   public void setId_msg(Long id_msg) {
      this.id_msg = id_msg;
   }

   public String getAddresses_to() {
      return addresses_to;
   }

   public void setAddresses_to(String addresses_to) {
      this.addresses_to = addresses_to;
   }

   public String getAddresses_cc() {
      return addresses_cc;
   }

   public void setAddresses_cc(String addresses_cc) {
      this.addresses_cc = addresses_cc;
   }

   public String getAddresses_bcc() {
      return addresses_bcc;
   }

   public void setAddresses_bcc(String addresses_bcc) {
      this.addresses_bcc = addresses_bcc;
   }

   public String getAddresses_from() {
      return addresses_from;
   }

   public void setAddresses_from(String addresses_from) {
      this.addresses_from = addresses_from;
   }
}
