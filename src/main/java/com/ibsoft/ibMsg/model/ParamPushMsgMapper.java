package com.ibsoft.ibMsg.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParamPushMsgMapper implements RowMapper<ParamPushMsg> {

   @Override
   public ParamPushMsg mapRow(ResultSet rs, int rowNum) throws SQLException {
      ParamPushMsg confParamMsg = new ParamPushMsg();
      confParamMsg.setId_msg(rs.getLong("mepa_mepu_id"));
      confParamMsg.setParamName(rs.getString("mepa_name"));
      confParamMsg.setParamValue(rs.getString("mepa_value"));
      return confParamMsg;
   }
}