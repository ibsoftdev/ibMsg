package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.config.ApplicationContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfMessageMapper implements RowMapper<ConfMessage> {

   JdbcTemplate jdbcTemplate;

   @Override
   public ConfMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
      ConfMessage confmsg = new ConfMessage();

      jdbcTemplate = ApplicationContextHolder.getContext().getBean(JdbcTemplate.class);

      confmsg.setNu_campana(rs.getInt("meme_meca_nu_campana"));
      confmsg.setNu_mensaje(rs.getInt("meme_nu_mensaje"));
      confmsg.setSubject(rs.getString("meme_tx_titulo"));
      confmsg.setBody(rs.getString("meme_tx_cuerpo"));
      confmsg.setTime_next(rs.getInt("meme_time_next"));

      confmsg.setChannel(getConfChannel(rs.getLong("meme_cd_channel")));

      return confmsg;
   }

   public ConfChannel getConfChannel(Long cd_channel){
      String sql = "SELECT mech_nu_channel, mech_url_server, mech_user_server,mech_class,mech_default_to FROM EXTRANET.MENT_CHANNEL WHERE mech_nu_channel = ?";
      ConfChannel confChannel = jdbcTemplate.queryForObject(sql, new ConfChannelMapper(), (Object[]) new Long[]{cd_channel});
      return confChannel;
   }

}