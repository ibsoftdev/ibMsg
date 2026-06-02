package com.ibsoft.ibMsg.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;

public class PushMessage {

   private Long    id;
   private String  subject;
   private String  body;
   private Integer nu_campana;
   private Integer nu_mensaje;
   private Integer nu_intento;
   private String status;//Indica el status de la ejecucion del ws
   @JsonFormat(pattern="dd-MM-yyyy HH:mm:ss", timezone="America/Caracas")
   private Date fe_inicio;
   List<ParamPushMsg> paramMsgs;
   List<AddresseePushMsg> addresseeMsgs;
   List<AddresseePushMsg> addresseeCCMsgs;
   List<AttachmentPushMsg> attachmentMsgs;

   @JsonIgnore
   ConfMessage confMsg;

   public PushMessage() {
   }

   public PushMessage(Long id, Integer nu_campana, Integer nu_mensaje, Integer nu_intento, Date fe_inicio) {
      this.id         = id;
      this.nu_campana = nu_campana;
      this.nu_mensaje = nu_mensaje;
      this.nu_intento = nu_intento;
      this.fe_inicio = fe_inicio;
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

   public Integer getNu_intento() { return nu_intento; }

   public void setNu_intento(Integer nu_intento) { this.nu_intento = nu_intento; }

   public Date getFe_inicio() { return fe_inicio; }

   public void setFe_inicio(Date fe_inicio) { this.fe_inicio = fe_inicio; }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public List<ParamPushMsg> getParamMsgs() {
      return paramMsgs;
   }

   public void setParamMsgs(List<ParamPushMsg> paramMsgs) {
      this.paramMsgs = paramMsgs;
   }

   public List<AddresseePushMsg> getAddresseeMsgs() {
      return addresseeMsgs;
   }

   public void setAddresseeMsgs(List<AddresseePushMsg> addresseeMsgs) {
      this.addresseeMsgs = addresseeMsgs;
   }

   public List<AddresseePushMsg> getAddresseeCCMsgs() {
      return addresseeCCMsgs;
   }

   public void setAddresseeCCMsgs(List<AddresseePushMsg> addresseeCCMsgs) {
      this.addresseeCCMsgs = addresseeCCMsgs;
   }

   public List<AttachmentPushMsg> getAttachmentMsgs() { return attachmentMsgs; }

   public void setAttachmentMsgs(List<AttachmentPushMsg> attachmentMsgs) { this.attachmentMsgs = attachmentMsgs; }

   @JsonIgnore
   public ConfMessage getConfMsg() {
      return confMsg;
   }

   public void setConfMsg(ConfMessage confMsg) {
      this.confMsg = confMsg;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }
}