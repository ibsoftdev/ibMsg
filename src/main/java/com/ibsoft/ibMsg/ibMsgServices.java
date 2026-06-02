package com.ibsoft.ibMsg;

import com.ibsoft.ibMsg.exception.MsgException;
import com.ibsoft.ibMsg.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Service
public class ibMsgServices {

   static Log logger = LogFactory.getLog(ibMsgServices.class.getName());

   @Autowired
   JdbcTemplate jdbcTemplate;

   @Autowired
   BeanFactory beanFactory;

   /*
   public List<ParamPushMsg> getParamMSGs(Long id_msg) throws Exception {
      String sql = "SELECT mepa_mepu_id, mepa_name, mepa_value FROM MENT_PARAM_PUSH_MSG WHERE mepa_mepu_id = ?";

      return jdbcTemplate.query(sql,
                                (rs, rowNum) ->
                                                 new ParamPushMsg(
                                                     rs.getLong("mepa_mepu_id"),
                                                     rs.getString("mepa_name"),
                                                     rs.getString("mepa_value")
                                  ),
                                new Long[]{id_msg}
                                );
   }
   */

   public ConfMessage getConfMessage(Integer nu_campana, Integer nu_mensaje) throws Exception {
      String sql = "SELECT meme_meca_nu_campana,meme_nu_mensaje,meme_tx_titulo,meme_tx_cuerpo,meme_cd_channel,meme_time_next FROM EXTRANET.MENT_MENSAJE WHERE meme_meca_nu_campana = ? AND meme_nu_mensaje = ?";
      ConfMessage confMessage = jdbcTemplate.queryForObject(sql, new ConfMessageMapper(), (Object[]) new Integer[]{nu_campana, nu_mensaje});
      return confMessage;
   }

   public PushMessage getPushMessage(Long id_msg) throws Exception {

      String sql = "SELECT mepu_meca_nu_campana, mepu_nu_mensaje,meme_tx_titulo,meme_tx_cuerpo,mepu_id,meme_time_next \n" +
                    "  FROM ment_push_msg,ment_mensaje \n" +
                     "    WHERE mepu_meca_nu_campana = meme_meca_nu_campana\n" +
                     "      and mepu_nu_mensaje = meme_nu_mensaje\n" +
                     "      and mepu_id = ?";

      PushMessage pushMessage = jdbcTemplate.queryForObject(sql, new PushMessageMapper(), (Object[]) new Long[]{id_msg});
      //pushMessage.setParamMsgs( getParamMSGs(pushMessage.getId()) );


      // Se busca la configuracion del Mensaje, y se reemplazan los
      // valores de los parametros en el cuerpo y en el asunto del mensaje.
      ConfMessage confMsg = getConfMessage(pushMessage.getNu_campana(), pushMessage.getNu_mensaje());
      String msgBody = confMsg.getBody();
      String msgSubject = confMsg.getSubject();
      for (ParamPushMsg paramMsg : pushMessage.getParamMsgs()) {
         msgBody = msgBody.replace("${"+paramMsg.getParamName()+"}", paramMsg.getParamValue());
         msgSubject = msgSubject.replace(paramMsg.getParamName(), paramMsg.getParamValue());
      }

      pushMessage.setBody(msgBody);
      pushMessage.setSubject(msgSubject);
      pushMessage.setConfMsg(confMsg);

      return pushMessage;
   }

   public List<PushMessage> getPendingMsgs(){

      String sql = "SELECT mepu_meca_nu_campana, mepu_nu_mensaje, meme_tx_titulo, meme_tx_cuerpo," +
            "              mepu_id, metr_nu_intento \n" +
            "   FROM ment_push_msg mpm, ment_mensaje mm, \n" +
            "        (SELECT mtpm1.metr_id, mtpm1.metr_nu_intento \n" +
            "           FROM ment_try_push_msg mtpm1 \n" +
            "           JOIN (SELECT metr_id, \n" +
            "                        max(metr_nu_intento) as max_intento \n" +
            "                   FROM ment_try_push_msg \n" +
            "               GROUP BY metr_id) mtpm2 \n" +
            "                     ON mtpm1.metr_id = mtpm2.metr_id \n" +
            "                    AND mtpm1.metr_nu_intento = mtpm2.max_intento \n" +
            "                    AND mtpm1.metr_status in ('P') \n" +
            "                 ) maxint \n" +
            "     WHERE mpm.mepu_meca_nu_campana = mm.meme_meca_nu_campana \n" +
            "       AND mpm.mepu_nu_mensaje = mm.meme_nu_mensaje \n" +
            "       AND mm.meme_cd_channel in (1,2,4) \n" +
            "       AND mpm.mepu_id = maxint.metr_id \n" +
            "       AND metr_nu_intento < meme_nu_intentos \n" +
            "       AND mepu_fe_status + NUMTODSINTERVAL(NVL(meme_time_next,0), 'second') <= SYSDATE \n" +
            "       AND NVL(mepu_fe_inicio,SYSDATE) <= SYSDATE \n" +
            "      ORDER BY mepu_fe_status DESC,mepu_id DESC";

      List<PushMessage> pushMessages  = jdbcTemplate.query(sql,
              new PushMessageMapper());

      for (PushMessage pushMsg:pushMessages){
         ConfMessage confMsg = null;
         try {
            confMsg = getConfMessage(pushMsg.getNu_campana(), pushMsg.getNu_mensaje());
            pushMsg.setConfMsg(confMsg);
            String msgBody = confMsg.getBody();
            String msgSubject = confMsg.getSubject();
            for (ParamPushMsg paramMsg : pushMsg.getParamMsgs()) {
               String paramName = "${"+paramMsg.getParamName()+"}";
               String paramValue = paramMsg.getParamValue()!=null?paramMsg.getParamValue():"";
               //logger.info("####### paramMsg : "+ paramName + "-" + paramValue);
               msgBody = msgBody.replace(paramName, paramValue);
               //la campania pudiera no tener titulo porque seria solo para sms
               if (msgSubject != null)
                  msgSubject = msgSubject.replace(paramName, paramValue);
            }
            pushMsg.setBody(msgBody);
            pushMsg.setSubject(msgSubject);
         }
         catch (Exception e) {
            logger.error("Error buscando mensaje:"+e);
         }
      }

      return pushMessages;
   }

   /*
    Cuando se inserta un Mensaje,  tambien se agregan los parametros con sus correspondientes valores.
    De esta forma se minimiza el espacio de almacenamiento.
    */
   /*
    Al definir el metodo completo como Transactional, cualquier Exception que
    se genere en el metodo hara que se dispare el Rollback. En caso de no generarse
    ningun error, se hara el Commit de manera automatica.
    El 'transactionManager' en un Bean definido en el AppInitializer.
    */
   @Transactional(transactionManager = "transactionManager")
   public PushMessage pushMessage(PushMessage pushMsg) throws Exception {

      //ConfMessage confMsg = getConfMessage(pushMsg.getNu_campana(), pushMsg.getNu_mensaje());
      /*
      Object[] values = new Object[]{nu_campana, nu_mensaje}; //, Calendar.getInstance().getTime()
      int[] types = new int[]{Types.INTEGER, Types.INTEGER}; //, Types.TIMESTAMP

      // execute insert query to insert the data
      // return number of row / rows processed by the executed query
      String insertSql = "INSERT into MENT_PUSH_MSG(MEPU_MECA_NU_CAMPANA,MEPU_NU_MENSAJE) values(?,?,?)";
      int row = jdbcTemplate.update(insertSql, values, types);

      KeyHolder keyHolder = new GeneratedKeyHolder();

      jdbcTemplate.update(
              new PreparedStatementCreator() {
                 public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
                    final PreparedStatement ps = connection.prepareStatement("INSERT into MENT_PUSH_MSG(MEPU_MECA_NU_CAMPANA,MEPU_NU_MENSAJE) values(?,?)", new String[] {"MEPU_ID"});
                    ps.setInt(1,nu_campana);
                    ps.setInt(2,nu_mensaje);
                    return ps;
                 }
              }, keyHolder);

      Long idPushMsg = keyHolder.getKey().longValue();
      */


      /*@TODO: Ejemplo de como Insertar un CLob
      String INSERT_STMT = "INSERT INTO MENT_PUSH_MSG (MEPU_ID,MEPU_MECA_NU_CAMPANA,MEPU_NU_MENSAJE,MEPU_TX_TITULO,MEPU_TX_CUERPO) VALUES (SEQ_MENT_MSG.NEXTVAL, :nu_campana, :nu_mensaje, :subject, :body)";
      MapSqlParameterSource paramSource = new MapSqlParameterSource();
      paramSource.addValue("nu_campana", nu_campana, Types.NUMERIC);
      paramSource.addValue("nu_mensaje", nu_mensaje, Types.NUMERIC);
      //paramSource.addValue("subject", msgSubject, Types.VARCHAR);
      //paramSource.addValue("body", msgBody, Types.CLOB);
      KeyHolder keyHolder = new GeneratedKeyHolder();
      namedParamTemplate.update(INSERT_STMT, paramSource, keyHolder, new String[] { "MEPU_ID" });
      int id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
      */


      // Se define el punto a partir del cual se desea se considere
      // la Transaccion para hacer RollBack. Todo lo anterior al este punto
      // por defecto hace commit de manera automatica.
      //TransactionStatus trxStatus = TransactionAspectSupport.currentTransactionStatus();
      //Object savepoint = trxStatus.createSavepoint();

      // La necesidad de usar el NamedParameterJdbcTemplate, es por que el mismo
      // permite asignar un KeyHolder, que sera usado para retornar el ID registrado
      // como secuencia automatica al momento de crear el registro.
      NamedParameterJdbcTemplate namedParamTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
      String INSERT_STMT = "INSERT INTO MENT_PUSH_MSG (mepu_id,mepu_meca_nu_campana,mepu_nu_mensaje,mepu_fe_status,mepu_fe_inicio) " +
            " VALUES (SEQ_MENT_MSG.NEXTVAL, :nu_campana, :nu_mensaje, sysdate, :fe_inicio)";
      MapSqlParameterSource paramSource = new MapSqlParameterSource();
      paramSource.addValue("nu_campana", pushMsg.getNu_campana(), Types.NUMERIC);
      paramSource.addValue("nu_mensaje", pushMsg.getNu_mensaje(), Types.NUMERIC);
      java.sql.Date fe_inicio_date = null;
      if (pushMsg.getFe_inicio() != null)
         fe_inicio_date = new java.sql.Date(pushMsg.getFe_inicio().getTime());
      paramSource.addValue("fe_inicio", fe_inicio_date, Types.TIMESTAMP);
      KeyHolder keyHolder = new GeneratedKeyHolder();
      namedParamTemplate.update(INSERT_STMT, paramSource, keyHolder, new String[]{"mepu_id"});
      Long idPushMsg = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : 0;

      Object[] values = new Object[]{idPushMsg, 0, "P"};
      int[] types = new int[]{Types.INTEGER, Types.INTEGER, Types.VARCHAR};
      String insertSql = "INSERT INTO MENT_TRY_PUSH_MSG(metr_id,metr_nu_intento,metr_status,metr_fe_status) VALUES(?,?,?,sysdate)";
      jdbcTemplate.update(insertSql, values, types);

      jdbcTemplate.batchUpdate("INSERT INTO ment_attachment_push_msg(meat_mepu_id,meat_path) values(?,?)", new BatchPreparedStatementSetter() {

         @Override
         public void setValues(PreparedStatement pStmt, int j) throws SQLException {
            AttachmentPushMsg attachmentPushMsg = pushMsg.getAttachmentMsgs().get(j);
            pStmt.setLong(1, idPushMsg);
            pStmt.setString(2, attachmentPushMsg.getFilePath());
         }

         @Override
         public int getBatchSize() {
            if (pushMsg.getAttachmentMsgs() == null)
               return 0;
            return pushMsg.getAttachmentMsgs().size();
         }

      });

      //try {
      jdbcTemplate.batchUpdate("INSERT INTO ment_param_push_msg(mepa_mepu_id,mepa_name,mepa_value) values(?,?,?)", new BatchPreparedStatementSetter() {

         @Override
         public void setValues(PreparedStatement pStmt, int j) throws SQLException {
            ParamPushMsg paramPushMsg = pushMsg.getParamMsgs().get(j);
            pStmt.setLong(1, idPushMsg);
            pStmt.setString(2, paramPushMsg.getParamName());
            pStmt.setString(3, paramPushMsg.getParamValue());
         }

         @Override
         public int getBatchSize() {
            if (pushMsg.getParamMsgs() == null)
               return 0;
            return pushMsg.getParamMsgs().size();
         }

      });
      //}
      /*catch(Exception ex){
         ex.printStackTrace();
         //trxStatus.rollbackToSavepoint(savepoint);
      }*/
      //finally {
      //trxStatus.releaseSavepoint(savepoint);
      //}

      jdbcTemplate.batchUpdate("INSERT INTO ment_addressee_push_msg(meap_mepu_id,meap_addresses_to,meap_addresses_from,meap_type) values(?,?,?,?)", new BatchPreparedStatementSetter() {

         @Override
         public void setValues(PreparedStatement pStmt, int j) throws SQLException {
            AddresseePushMsg addresseePushMsg = pushMsg.getAddresseeMsgs().get(j);
            pStmt.setLong(1, idPushMsg);
            pStmt.setString(2, addresseePushMsg.getAddresses_to());
            pStmt.setString(3, addresseePushMsg.getAddresses_from());
            pStmt.setInt(4, addresseePushMsg.getType());
         }

         @Override
         public int getBatchSize() {
            return pushMsg.getAddresseeMsgs().size();
         }

      });

      pushMsg.setId(idPushMsg);
      return pushMsg;
   }

   @Transactional(transactionManager = "transactionManager")
   public void dispatchPendingMsg(){
      List<PushMessage> messageList = null;
      try {
         messageList = getPendingMsgs();
         for (PushMessage msg: messageList) {
            try {
               //Actualiza el numero de intento de envio de mensaje
               updatePushMsgSendAttemp(msg);
               //Envia el mensaje
               msg.getConfMsg().getChannel().sendMessage(msg);
               //Si no se presenta ninguna MsgException se marca el mensaje como enviado
               updatePushMsgStatus(msg, "E", "");
            }
            catch (MsgException me){
               logger.error("error enviando mensaje pendiente: "+me.getMessage()+" id:"+msg.getId());
               //Continua como pendiente por falla de envio
               updatePushMsgStatus(msg, "P", me.getMessage());
            }
         }
      }
      catch(Exception ex){
         logger.error("error en ejecucion de mensajes pendientes: "+ex.getMessage());
      }
   }

   public void updatePushMsgStatus(PushMessage pushMsg, String status, String error){
      Object[] values = new Object[]{status, (error.length()>600?error.substring(0,600):error),
            pushMsg.getId(), pushMsg.getNu_intento()};
      int[] types = new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER};
      String insertSql = "UPDATE MENT_TRY_PUSH_MSG SET metr_status = ?, metr_fe_status = sysdate, " +
            " metr_de_error = ?"+
            " WHERE metr_id = ? and metr_nu_intento = ?";
      int row = jdbcTemplate.update(insertSql, values, types);
      if (row == 0){
         logger.error("Error actualizando intento de envio mensaje con ID:"+pushMsg.getId()+" intento:"+pushMsg.getNu_intento());
      }
   }

   public void updatePushMsgSendAttemp(PushMessage pushMsg){
      Object[] values = new Object[]{pushMsg.getId(), pushMsg.getNu_intento()+1, "P"};
      int[] types = new int[]{Types.INTEGER, Types.INTEGER, Types.VARCHAR};
      String insertSql = "INSERT INTO MENT_TRY_PUSH_MSG(metr_id,metr_nu_intento," +
            "metr_status,metr_fe_status) VALUES(?,?,?,sysdate)";
      int row = jdbcTemplate.update(insertSql, values, types);
      if (row == 0){
         logger.error("Error incrementando numero de intento de envio de mensaje con ID:"+pushMsg.getId()+" intento:"+pushMsg.getNu_intento()+1);
      }
      else {
         //al registrarse adecuadamente se actualiza el numero de intento actual
         pushMsg.setNu_intento(pushMsg.getNu_intento() + 1);
      }
   }

   /*
    * Verifica si el string es vacio
    * */
   public boolean isNull(Object str) {
      return (null == str) || (str.toString().length() <= 0);
   }

}
