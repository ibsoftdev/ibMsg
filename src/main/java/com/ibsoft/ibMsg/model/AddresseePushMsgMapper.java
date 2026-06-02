package com.ibsoft.ibMsg.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AddresseePushMsgMapper implements RowMapper<AddresseePushMsg> {

   @Override
   public AddresseePushMsg mapRow(ResultSet rs, int rowNum) throws SQLException {
      AddresseePushMsg  addresseePushMsg = new AddresseePushMsg();
      addresseePushMsg.setId_msg(rs.getLong("mepa_mepu_id"));
      addresseePushMsg.setAddresses_to(rs.getString("addresses_to"));
      addresseePushMsg.setAddresses_from(rs.getString("addresses_from"));
      return addresseePushMsg;
   }
}