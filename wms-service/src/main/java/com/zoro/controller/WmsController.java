package com.zoro.controller;

import com.alibaba.fastjson.JSONObject;
import com.zoro.Service.WmsService;
import com.zoro.config.mq.WmsProducter;
import com.zoro.persistence.Wms;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
public class WmsController {

    @Autowired
    private WmsProducter wmsProducter ;

    @Autowired
    private WmsService wmsService ;

    @Value("${mq.callbakOrderWmsTopic}")
    private String callbakOrderWms;

    public void createWms(@RequestBody Wms wms){
        log.info("物流信息为:{}",wms);
        int result = wmsService.createWms(wms);
        if(result == 1){
            //发送
            Message message = new Message();
            message.setTopic(callbakOrderWms);
            message.setBody(JSONObject.toJSONString(wms.getOrderId()).getBytes());
            /*SendResult sendResult = wmsProducter.sendMessage(message);
            if (!sendResult.getSendStatus().equals(SendStatus.SEND_OK)){
                log.error("发送信息失败,持久化到数据库，人工处理，失败数据为：{}",wms);
            }*/
        }


    }
}
