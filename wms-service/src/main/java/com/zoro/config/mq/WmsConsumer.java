package com.zoro.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.zoro.Service.WmsService;
import com.zoro.persistence.Wms;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WmsConsumer {

    @Value("${mq.nameserver}")
    private String nameSvr;

    @Value("${mq.groupname}")
    private String groupName;

    @Value("${mq.topic}")
    private String topic;

    @Value("${mq.callbakOrderWmsTopic}")
    private String callbakOrderWms;//回调订单更新状态

    private  DefaultMQPushConsumer consumer ;

    @Autowired
    private WmsService wmsService ;

    @Autowired
    private WmsProducter wmsProducter ;

    @PostConstruct
    public void init() throws MQClientException {
        this.consumer = new DefaultMQPushConsumer(groupName);
        this.consumer.setNamesrvAddr(nameSvr);
        this.consumer.setConsumeThreadMin(10);
        this.consumer.setConsumeThreadMax(20);
        this.consumer.subscribe(topic,"*");
        this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        this.consumer.registerMessageListener(new WmsMessageListener());
        try {
            this.consumer.start();
        } catch (MQClientException e) {
            log.error("启动消费组:{},主题{}的消费者失败，失败信息{}",groupName,topic,e);
            e.printStackTrace();
        }

    }

    class WmsMessageListener implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

            for (MessageExt messageExt:msgs) {
                try {
                    String body = new String(messageExt.getBody());
                    log.info("消息体:{}", JSONObject.toJSONString(body));
                    int orderid = JSONObject.parseObject(body, int.class);

                    Message message = new Message();
                    message.setTopic(callbakOrderWms);
                    Map<String,String> map = new HashMap<>();
                    map.put("orderId",String.valueOf(orderid)) ;
                    map.put("wms_status","1");
                    message.setBody(JSONObject.toJSONString(map).getBytes());
                    TransactionSendResult sendResult = wmsProducter.sendMessage(message,orderid);

                    if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                        log.error("发送信息失败,持久化到数据库，人工处理，失败数据为：订单号：{}  物流状态:{}",orderid,1);
                    }
                }catch (Exception e){
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER ;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }
}
