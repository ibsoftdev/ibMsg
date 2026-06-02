package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.config.ApplicationContextHolder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfChannelMapper implements RowMapper<ConfChannel> {


   @Override
   public ConfChannel mapRow(ResultSet rs, int rowNum) throws SQLException {

      ConfChannel confChannel =  ApplicationContextHolder.getContext().getBean(rs.getString("mech_class"),ConfChannel.class);

      confChannel.setChannel(rs.getLong("mech_nu_channel"));
      confChannel.setUrlServer(rs.getString("mech_url_server"));
      confChannel.setUserServer(rs.getString("mech_user_server"));
      confChannel.setDefaultTo(rs.getString("mech_default_to"));

      return confChannel;
   }
}