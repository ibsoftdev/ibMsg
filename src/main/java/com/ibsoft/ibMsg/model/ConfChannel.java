package com.ibsoft.ibMsg.model;


import com.ibsoft.ibMsg.exception.MsgException;
import org.springframework.stereotype.Service;

@Service("ChannelService")
public abstract class ConfChannel {

   private String urlServer;
   private String userServer;
   private Long   channel;
   private String defaultTo;

   public ConfChannel() {
   }

   public ConfChannel(String urlServer, String userServer, Long channel, String defaultTo) {
      this.urlServer = urlServer;
      this.userServer = userServer;
      this.channel = channel;
      this.defaultTo = defaultTo;
   }

   public String getUrlServer() {
      return urlServer;
   }

   public void setUrlServer(String urlServer) {
      this.urlServer = urlServer;
   }

   public String getUserServer() {
      return userServer;
   }

   public void setUserServer(String userServer) {
      this.userServer = userServer;
   }

   public Long getChannel() {
      return channel;
   }

   public void setChannel(Long channel) {
      this.channel = channel;
   }

   public String getDefaultTo() { return defaultTo; }

   public void setDefaultTo(String defaultTo) { this.defaultTo = defaultTo; }

   public abstract void sendMessage(PushMessage pushMsg) throws MsgException;
}