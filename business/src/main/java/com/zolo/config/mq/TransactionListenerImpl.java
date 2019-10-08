package com.zolo.config.mq;

import com.zolo.entity.CommonDto;
import com.zolo.feign.OrderFeignClient;
import com.zolo.feign.StorageFeignClient;
import com.zolo.service.BusinessService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TransactionListenerImpl implements TransactionListener {
    /*@Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;
*/
    @Autowired
    BusinessService businessService;

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("business-transactionListenerImpl全局事务Id{}",RootContext.getXID());
        try {
            Map<String, CommonDto> parameterMap = (Map<String,CommonDto>) arg;
            businessService.purchase(parameterMap.get("dto"));
        }catch (Exception e){
            log.error("MQ执行本地事务失败:{}",e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        return LocalTransactionState.COMMIT_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
