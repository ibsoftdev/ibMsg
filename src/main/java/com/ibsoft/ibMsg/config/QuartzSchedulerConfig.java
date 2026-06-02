package com.ibsoft.ibMsg.config;

import com.ibsoft.ibMsg.model.PendingsMsgsJob;
import org.springframework.beans.factory.annotation.Value;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzSchedulerConfig {

   @Value("${com.ibsoft.ibMsgEnv.executeFrequencyInSec:60}")
   private int executeFrequencyInSec;

   @Value("${com.ibsoft.ibMsgEnv.startUpDelay:10}")
   private int startupDelay;

   @Autowired
   private ApplicationContext applicationContext;

   @Bean
   public SpringBeanJobFactory springBeanJobFactory() {
      AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
      jobFactory.setApplicationContext(applicationContext);
      return jobFactory;
   }

   @Bean
   public JobDetail pendingsMsgsJobDetail() {
      return createJobDetail("PendingsMsgsJobDetail", PendingsMsgsJob.class,
            "gestion de mensajes pendientes");
   }

   @Bean
   public Trigger pendingsMsgsJobTrigger(JobDetail pendingsMsgsJobDetail) {
      SimpleTriggerFactoryBean triggerFactory = new SimpleTriggerFactoryBean();
      triggerFactory.setJobDetail(pendingsMsgsJobDetail);
      triggerFactory.setRepeatInterval(executeFrequencyInSec * 1000L);
      triggerFactory.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
      triggerFactory.setName("PendingsMsgsJobTrigger");
      triggerFactory.afterPropertiesSet();
      return triggerFactory.getObject();
   }

   @Bean
   public Scheduler scheduler(Trigger pendingsMsgsJobTrigger, JobDetail pendingsMsgsJobDetail)
         throws Exception {
      SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
      schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
      schedulerFactory.setJobFactory(springBeanJobFactory());
      schedulerFactory.setJobDetails(pendingsMsgsJobDetail);
      schedulerFactory.setTriggers(pendingsMsgsJobTrigger);
      schedulerFactory.setStartupDelay(startupDelay);
      schedulerFactory.afterPropertiesSet();
      schedulerFactory.start();
      return schedulerFactory.getObject();
   }

   private JobDetail createJobDetail(String jobDetailName, Class<? extends Job> jobClass,
         String description) {
      JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
      jobDetailFactory.setJobClass(jobClass);
      jobDetailFactory.setName(jobDetailName);
      jobDetailFactory.setDescription(description);
      jobDetailFactory.setDurability(true);
      jobDetailFactory.afterPropertiesSet();
      return jobDetailFactory.getObject();
   }
}
