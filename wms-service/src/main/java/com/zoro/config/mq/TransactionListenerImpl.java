package com.zoro.config.mq;

import com.zoro.Service.WmsService;
import com.zoro.persistence.Wms;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class TransactionListenerImpl implements TransactionListener {


    @Autowired
    private  WmsService wmsService;
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String orderNO = (String) arg;
        Wms result = wmsService.selectWmsByOrderNo(orderNO);
        if(result != null){
            log.info("物流信息已存在{},直接投递消息到订单服务，更新订单的物流状态",result);
            return LocalTransactionState.COMMIT_MESSAGE;
        }
        Wms wms = new Wms() ;
        wms.setAddress("test");
        wms.setOrderNO(orderNO);
        wms.setCreateTime(new Date());
        wms.setUpdateTime(new Date());
        wms.setWmsStatus("1");
        log.info("物流信息{}",wms);
        try {
            int wmsResult = wmsService.createWms(wms);
            if (wmsResult == 1){
                return LocalTransactionState.COMMIT_MESSAGE;
            }else {
                log.info("回滚信息",wms);
                return LocalTransactionState.ROLLBACK_MESSAGE ;
            }

        } catch (Exception e) {
            e.printStackTrace();
            //发生异常重新投递消息
            return LocalTransactionState.UNKNOW ;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.info("消息回查{}",msg);
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
