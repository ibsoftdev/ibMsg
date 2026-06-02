package com.ibsoft.ibMsg.model;

public class ParamPushMsg {

   private Long   id_msg;
   private String paramName;
   private String paramValue;

   public ParamPushMsg() {
   }

   public ParamPushMsg(Long id_msg, String paramName, String paramValue) {
      this.id_msg = id_msg;
      this.paramName = paramName;
      this.paramValue = paramValue;
   }

   public Long getId_msg() {
      return id_msg;
   }

   public void setId_msg(Long id_msg) {
      this.id_msg = id_msg;
   }

   public String getParamName() {
      return paramName;
   }

   public void setParamName(String paramName) {
      this.paramName = paramName;
   }

   public String getParamValue() {
      return paramValue;
   }

   public void setParamValue(String paramValue) {
      this.paramValue = paramValue;
   }
}