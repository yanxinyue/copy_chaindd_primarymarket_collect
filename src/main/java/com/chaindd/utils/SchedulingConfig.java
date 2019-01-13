package com.chaindd.utils;

import com.chaindd.service.ICObenchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.io.IOException;

/**
 * 定时任务配置
 * @Author: xinyueyan
 * @Date: 9/7/2018 2:34 PM
 */
@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig{
    @Autowired
    private ICObenchService icoService;

    //@Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点执行
    public void getDataFromICObench() {
        //定时更新ICObench项目列表
        try {
            log.info("=====START: update ico list from ICObench");
            icoService.getData();
            log.info("=====END: update ico list from ICObench");
        } catch (IOException e) {
            log.error("SchedulingConfig getList from ICObench error: ", e);
        }

        /*try{
            log.info("=====START: update ico info from ICObench");
            icoService.getICOinfo();
            log.info("=====START: update ico info from ICObench");
        }catch (Exception e){
            log.error("SchedulingConfig getInfo from ICObench error: ", e);

        }*/
    }

}