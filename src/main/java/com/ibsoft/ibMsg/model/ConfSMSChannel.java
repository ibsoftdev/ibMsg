package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.exception.MsgException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Service("SMSChannelService")
public class ConfSMSChannel extends ConfChannel {

   private static Log logger = LogFactory.getLog(ConfSMSChannel.class);

   private final JsonMapper jsonMapper = JsonMapper.builder().build();

   @Value("${com.ibsoft.ibMsgEnv.app_env:DE}")
   private String appEnv;

   @Override
   public void sendMessage(PushMessage pushMsg) throws MsgException {
      logger.info("ConfSMSChannel.sendMessage");

      for (AddresseePushMsg addr: pushMsg.getAddresseeMsgs()){
         logger.info("To:"+addr.getAddresses_to()+" From:"+addr.getAddresses_from());
         sendSMS(pushMsg, addr);
      }

   }

   public void sendSMS(PushMessage pushMsg, AddresseePushMsg addr) throws MsgException{
      try{
         //Esto se hace para no validar el certificado. En este caso no esta firmado
         SSLContextBuilder builder = new SSLContextBuilder();
         builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
         SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
         CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

         String celdestino = addr.getAddresses_to();
         if ((getDefaultTo() != null) && (getDefaultTo().length()>0))
            celdestino = getDefaultTo();
         else if (!"PR".equalsIgnoreCase(appEnv))
            throw new MsgException("Se debe configurar un 'default_to' para el ambiente de Desarrollo");

         String[] cel_split = celdestino.split(";");

         for (int i = 0; i < cel_split.length; i++){
            try {
               Map<String, String> values = new HashMap<>();
               values.put("mensaje", pushMsg.getBody());
               values.put("num_dest", cel_split[i]);
               values.put("prior", "0");
               values.put("dep", "01");
               values.put("id", new SimpleDateFormat("yyyyMMddHHSSmm").format(new Date()));

               StringEntity entity = new StringEntity(jsonMapper.writeValueAsString(values));
               entity.setContentType("application/json");

               URI uri = new URI(getUrlServer());

               String serverApikey = getUserServer();
               HttpPost post = new HttpPost(uri);
               post.addHeader("Content-Type", "application/json");
               post.addHeader("X-Auth-Apikey", serverApikey);
               post.setEntity(entity);

               CloseableHttpResponse response = httpclient.execute(post);
               String respuesta = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
               logger.info("respuesta SMS:" + respuesta);
               Map<String, String> respuestaJson = jsonMapper.readValue(
                     respuesta, new TypeReference<Map<String, String>>() {});
               if (!"OK".equalsIgnoreCase(respuestaJson.get("status"))) {
                  throw new MsgException(respuesta);
               }

            }
            catch (Exception e){
               logger.error("No pudo ser enviado el mensaje", e);
               throw new MsgException(e.getMessage());
            }
         }

      }
      catch (Exception e){
         logger.error("No pudo ser enviado el mensaje", e);
         throw new MsgException(e.getMessage());
      }
   }
}
