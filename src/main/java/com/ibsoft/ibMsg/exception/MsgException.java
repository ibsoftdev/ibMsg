/**
 * Title:        MsgException <p>
 * Description:  .<p>
 * Copyright:    (c) 2000. ib-Soft Development<p>
 * Company:      ib-Soft Development<p>
 *
 * @author Luis Rodrigues
 * @version 1.0
 * @since JDK1.8
 *        Created: 13-12-2022
 *        <p/>
 *        MsgException
 */

package com.ibsoft.ibMsg.exception;

public class MsgException extends Exception {

   public MsgException(){
      super();
   }

   public MsgException(String msg, Throwable cause) {
      super(msg,cause);
   }

   public MsgException(String msg) {
      super(msg);
   }
}