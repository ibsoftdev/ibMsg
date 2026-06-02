package com.ibsoft.ibMsg.model;

public class AttachmentPushMsg {

   private Long   id_msg;
   private String filePath;

   public AttachmentPushMsg() {
   }

   public AttachmentPushMsg(Long id_msg, String filePath) {
      this.id_msg = id_msg;
      this.filePath = filePath;
   }

   public Long getId_msg() {
      return id_msg;
   }

   public void setId_msg(Long id_msg) {
      this.id_msg = id_msg;
   }

   public String getFilePath() { return filePath; }

   public void setFilePath(String filePath) { this.filePath = filePath; }

}