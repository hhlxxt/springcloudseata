package com.zolo.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.zolo.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CallbackWmsConsumer {
    @Value("${mq.nameserver}")
    private String nameSvr;

    @Value("${mq.groupname}")
    private String groupName;

    @Value("${mq.callbakOrderWmsTopic}")
    private String topic;

    private DefaultMQPushConsumer consumer ;

    @Autowired
    private OrderService orderService ;


    @PostConstruct
    public void init() throws MQClientException {
        this.consumer = new DefaultMQPushConsumer(groupName);
        this.consumer.setNamesrvAddr(nameSvr);
        this.consumer.setConsumeThreadMin(10);
        this.consumer.setConsumeThreadMax(20);
        this.consumer.subscribe(topic,"*");
        this.consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        this.consumer.registerMessageListener(new CallbackWmsMessageListener());
        try {
            this.consumer.start();
        } catch (MQClientException e) {
            log.error("启动消费组:{},主题{}的消费者失败，失败信息{}",groupName,topic,e);
            e.printStackTrace();
        }

    }

    class CallbackWmsMessageListener implements MessageListenerConcurrently {
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            for (MessageExt messageExt:msgs) {
                try {                    String body = new String(messageExt.getBody());
                    log.info("消息体:{}", JSONObject.toJSONString(body));
                    Map<String,String> map = JSONObject.parseObject(body, Map.class);
                    log.info("物流信息{},更新订单状态",map);
                    String orderId = map.get("orderId");
                    String wms_status = map.get("wms_status");
                    int result = orderService.updateOrderStatusById(Integer.parseInt(orderId), wms_status);
                    if(result == 0){
                        log.error("更新订单状态失败,订单号{}",orderId);
                        throw new RuntimeException("更新订单状态失败,订单号{"+orderId+"}");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    log.error("更新订单信息失败，订单信息：",messageExt);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER ;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }
}
