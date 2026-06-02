package com.ibsoft.ibMsg.model;

import com.ibsoft.ibMsg.ibMsgServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@DisallowConcurrentExecution
public class PendingsMsgsJob implements Job
{
   static Log logger = LogFactory.getLog(PendingsMsgsJob.class.getName());

   @Autowired
   private ibMsgServices services;

   public void execute(JobExecutionContext context) throws JobExecutionException {

      logger.info("PendingsMsgsJob ** "+context.getJobDetail().getKey().getName()+" ** fired @ " + context.getFireTime());

      services.dispatchPendingMsg();

      logger.info("Next PendingsMsgsJob scheduled @ " + context.getNextFireTime());
   }


}