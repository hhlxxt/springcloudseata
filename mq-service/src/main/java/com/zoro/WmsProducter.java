package com.zoro;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 物流生产者
 */
@Component
@Slf4j
public class WmsProducter {

    private DefaultMQProducer producer ;

    @Value("${mq.nameserver}")
    private String nameSvr;

    @Value("${mq.sendMsgTimeout}")
    private  int timeOut ;

    @Value("${mq.groupname}")
    private String groupName;


    @Value("${mq.producer.retryTimesWhenSendFailed}")
    private Integer retryTimes;

    @PostConstruct
    public void init(){
        producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(nameSvr);
        producer.setRetryTimesWhenSendFailed(retryTimes);
        producer.setSendMsgTimeout(timeOut);
        try {
            this.producer.start();
        } catch (MQClientException e) {
            log.error("mq-Service start producter fail ",e);
            e.printStackTrace();
        }
    }

    /**
     * 发生物流信息
     *
     * @param message
     */
    public void sendMessage(Message message){
        try {
            SendResult send = producer.send(message);
            log.info("物流信息发生结果"+send);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
