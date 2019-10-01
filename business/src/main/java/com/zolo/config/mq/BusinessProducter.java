package com.zolo.config.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 *订单链路成功后发送物流信息
 */
@Component
@Slf4j
public class BusinessProducter implements InitializingBean {
    private TransactionMQProducer producer ;

    private ExecutorService executorService ;

    @Autowired
    TransactionListenerImpl transactionListener ;

    @Value("${mq.nameserver}")
    private String nameSvr;

    @Value("${mq.groupname}")
    private String groupName;

    @Value("${mq.sendMsgTimeout}")
    private  int timeOut ;


    @Value("${mq.producer.retryTimesWhenSendFailed}")
    private Integer retryTimes;

    @PostConstruct
    public void init(){
        executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("business-transaction-msg-check-thread");
                return thread;
            }
        });
        producer = new TransactionMQProducer(groupName);
        producer.setExecutorService(executorService);
        producer.setNamesrvAddr(nameSvr);
        producer.setRetryTimesWhenSendFailed(retryTimes);
        producer.setSendMsgTimeout(timeOut);

    }

    /**
     * 通知仓储发货
     *
     * @param message
     * @param arg
     */
    public SendResult sendMessage(Message message, Object arg){
        try {
            TransactionSendResult sendResult = this.producer.sendMessageInTransaction(message, arg);
            System.err.println(new String(message.getBody()));
            log.info("通知仓储发货，投递消息结果: \t "+sendResult);
            return sendResult;
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        producer.setTransactionListener(transactionListener);
        try {
            this.producer.start();
        } catch (MQClientException e) {
            log.error("mq-Service start producter fail ",e);
            e.printStackTrace();
        }
    }
}
