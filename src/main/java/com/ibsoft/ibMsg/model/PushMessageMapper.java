package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.config.ApplicationContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PushMessageMapper implements RowMapper<PushMessage> {

   JdbcTemplate jdbcTemplate;

   @Override
   public PushMessage mapRow(ResultSet rs, int rowNum) throws SQLException {

      jdbcTemplate = ApplicationContextHolder.getContext().getBean(JdbcTemplate.class);

      PushMessage pushmsg = new PushMessage();

      pushmsg.setNu_campana(rs.getInt("mepu_meca_nu_campana"));
      pushmsg.setNu_mensaje(rs.getInt("mepu_nu_mensaje"));
      pushmsg.setSubject(rs.getString("meme_tx_titulo"));
      pushmsg.setBody(rs.getString("meme_tx_cuerpo"));
      pushmsg.setId(rs.getLong("mepu_id"));
      pushmsg.setNu_intento(rs.getInt("metr_nu_intento"));

      pushmsg.setParamMsgs(getParamMSGs(pushmsg.getId()));
      pushmsg.setAddresseeMsgs(getAddressee(pushmsg.getId()));
      pushmsg.setAddresseeCCMsgs(getAddresseeCC(pushmsg.getId()));
      pushmsg.setAttachmentMsgs(getAttachmentMSGs(pushmsg.getId()));

      return pushmsg;
   }

   public List<ParamPushMsg> getParamMSGs(Long id_msg){
      String sql = "SELECT mepa_mepu_id, mepa_name, mepa_value FROM MENT_PARAM_PUSH_MSG WHERE mepa_mepu_id = ?";

      List<ParamPushMsg> paramPushMsgsList = jdbcTemplate.query(sql,
              (rs, rowNum) ->
                      new ParamPushMsg(
                              rs.getLong("mepa_mepu_id"),
                              rs.getString("mepa_name"),
                              rs.getString("mepa_value")
                      ),
              (Object[]) new Long[]{id_msg}
      );

      return paramPushMsgsList;
   }

   public List<AddresseePushMsg> getAddressee(Long id_msg){
      String sql = "SELECT meap_mepu_id, meap_addresses_to, meap_addresses_from,meap_type FROM MENT_ADDRESSEE_PUSH_MSG WHERE meap_type = 1 AND meap_mepu_id = ?";

      return jdbcTemplate.query(sql,
              (rs, rowNum) ->
                      new AddresseePushMsg(
                              rs.getLong("meap_mepu_id"),
                              rs.getString("meap_addresses_to"),
                              rs.getString("meap_addresses_from"),
                              rs.getInt("meap_type")
                      ),
              (Object[]) new Long[]{id_msg}
      );
   }

   public List<AddresseePushMsg> getAddresseeCC(Long id_msg){
      String sql = "SELECT meap_mepu_id, meap_addresses_to, meap_addresses_from,meap_type FROM MENT_ADDRESSEE_PUSH_MSG WHERE meap_type = 2 AND meap_mepu_id = ?";

      return jdbcTemplate.query(sql,
              (rs, rowNum) ->
                      new AddresseePushMsg(
                              rs.getLong("meap_mepu_id"),
                              rs.getString("meap_addresses_to"),
                              rs.getString("meap_addresses_from"),
                              rs.getInt("meap_type")
                      ),
              (Object[]) new Long[]{id_msg}
      );
   }

   public List<AttachmentPushMsg> getAttachmentMSGs(Long id_msg){
      String sql = "SELECT meat_mepu_id, meat_path FROM MENT_ATTACHMENT_PUSH_MSG WHERE meat_mepu_id = ?";

      List<AttachmentPushMsg> attachmentPushMsgsList = jdbcTemplate.query(sql,
            (rs, rowNum) ->
                  new AttachmentPushMsg(
                        rs.getLong("meat_mepu_id"),
                        rs.getString("meat_path")
                  ),
              (Object[]) new Long[]{id_msg}
      );

      return attachmentPushMsgsList;
   }

}