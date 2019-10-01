package com.zoro;

import com.alibaba.fastjson.JSONObject;
import com.zoro.Service.WmsService;
import com.zoro.config.mq.WmsConsumer;
import com.zoro.config.mq.WmsProducter;
import com.zoro.persistence.Wms;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WmsTest {
    @Autowired
    private WmsService wmsService ;

    @Autowired
    private WmsProducter wmsProducter ;

    @Autowired
    private WmsConsumer wmsConsumer ;

    @Test
    public void createWms(){
        Wms wms = new Wms() ;
        wms.setAddress("test");
        wms.setOrderId(1001);
        wms.setWmsStatus("0");
        wms.setCreateTime(new Date());
        wms.setUpdateTime(new Date());
        wmsService.createWms(wms);
    }
    @Test
    public void selectWmsByOrderId(){

        Wms wms = wmsService.selectWmsByOrderId(77);
        System.err.println(wms);
    }

    @Test
    public void sendMessage(){
        Message message = new Message();
        message.setTopic("wms-topic");
        Wms wms = new Wms() ;
        wms.setAddress("test");
        wms.setOrderId(1001);
        wms.setWmsStatus("0");
        wms.setCreateTime(new Date());
        wms.setUpdateTime(new Date());

        message.setBody(JSONObject.toJSONString(wms).getBytes());
        wmsProducter.sendMessage(message,1001);

    }

    @Test
    public void Json()  {
        String jsonString = JSONObject.toJSONString(111);
        System.err.println("jsonString"+jsonString);
        int orderId = JSONObject.parseObject(jsonString, int.class);
        System.err.println("orderId"+orderId);

    }
}
