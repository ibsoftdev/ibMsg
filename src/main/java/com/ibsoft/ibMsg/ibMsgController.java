package com.ibsoft.ibMsg;

import com.ibsoft.ibMsg.model.ConfChannel;
import com.ibsoft.ibMsg.model.GetMsgRequest;
import com.ibsoft.ibMsg.model.PushMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ibMsgController {

   static Log logger = LogFactory.getLog(ibMsgController.class.getName());

   @Autowired
   ibMsgServices services;

   /*
    {
    "id": null,
    "subject": null,
    "body": null,
    "channel": null,
    "nu_campana": 10,
    "nu_mensaje": 10,
    "fe_inicio": '14-03-2023 16:00:00',
    "addresseeMsgs": [
              { "addresses_to":"cuchivano@gmail.com",
                "addresses_from":"ibsoftdev.info@gmail.com"
                "type":1
              },
              { "addresses_to":"cuchivano@yahoo.com",
                "addresses_from":"ibsoftdev.info@gmail.com"
                "type":2
              }
             ],
    "paramMsgs":[
              { "paramName":"ID_SOLICITUD",
                "paramValue":"23062022"
              },
              { "paramName":"FECHA_SOLICITUD",
                "paramValue":"10/10/2022"
              }
             ]
    }
    */

   @RequestMapping(value = "/pushMsg", method = RequestMethod.POST,
         consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Object> pushMsg(@RequestBody PushMessage pushMsg) {
      PushMessage pushMessage = null;
      try {

         // Extraer un Map de Json
         //Parametro:@RequestBody ObjectNode json
         //ObjectMapper mapper = new ObjectMapper();
         //JsonNode jsonParams = json.get("params");
         //List<ParamPushMsg> paramPushMsgList = mapper.convertValue(jsonParams, new TypeReference<List<ParamPushMsg>>(){});
         //Map<String, String> params = mapper.convertValue(jsonParams, new TypeReference<Map<String, String>>() {});

         pushMessage = services.pushMessage(pushMsg);
         pushMessage.setStatus("Success");
      }
      catch(Exception ex){
         logger.error(ex.getMessage(),ex);
         pushMessage.setStatus("Error");
      }
      return new ResponseEntity<>(pushMessage, HttpStatus.OK);
   }

   /*
     {
       "id_msg": 201
     }
    */
   @RequestMapping(value = "/getMsg", method = RequestMethod.POST, headers = {"Content-type=application/json","Accept=application/json"})
   public ResponseEntity<Object> getMsg(@RequestBody GetMsgRequest request) {
      PushMessage pushMsg = null;
      try {
         pushMsg = services.getPushMessage(request.getId_msg());
         ConfChannel confChannel = pushMsg.getConfMsg().getChannel();
         confChannel.sendMessage(pushMsg);
         pushMsg.setStatus("Success");
      }
      catch(Exception ex){
         logger.error(ex.getMessage(),ex);
         pushMsg.setStatus("Error");
      }
      return new ResponseEntity<>(pushMsg, HttpStatus.OK);
   }

   @RequestMapping(value = "/getPendingMsgs", method = RequestMethod.POST, headers = {"Content-type=application/json"})
   public ResponseEntity<Object> getPendingMsgs() {
      List<PushMessage> messageList = null;
      try {
         messageList = services.getPendingMsgs();
         for (PushMessage msg: messageList)
            msg.getConfMsg().getChannel().sendMessage(msg);

      }
      catch(Exception ex){
         logger.error(ex.getMessage(),ex);
      }
      return new ResponseEntity<>(messageList, HttpStatus.OK);
   }
}
