package com.zolo.config.mq;

import com.zolo.feign.OrderFeignClient;
import com.zolo.feign.StorageFeignClient;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionListenerImpl implements TransactionListener {
    @Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("business-transactionListenerImpl全局事务Id{}",RootContext.getXID());
        int orderId = (int) arg;
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
