package com.zolo.service;

import com.zolo.config.mq.BusinessProducter;
import com.zolo.feign.OrderFeignClient;
import com.zolo.feign.StorageFeignClient;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;


@Service
@Slf4j
public class BusinessService {

    @Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BusinessProducter businessProducter ;

    @Value("${mq.topic}")
    private String topic;

    /**
     * 减库存，下订单
     *
     * @param userId
     * @param commodityCode
     * @param orderCount
     */
    @GlobalTransactional
    public void purchase(String userId, String commodityCode, int orderCount) {
        log.info("BusinessService全局事务Id{}", RootContext.getXID());
        storageFeignClient.deduct(commodityCode, orderCount);

        int orderId = orderFeignClient.create(userId, commodityCode, orderCount);

        if (!validData()) {
            throw new RuntimeException("账户或库存不足,执行回滚");
        }

        //投递消息到mq 通知发货
        try{
            Message message = new Message();
            message.setTopic(topic);
            message.setBody(String.valueOf(orderId).getBytes());
            SendResult sendResult = businessProducter.sendMessage(message,orderId);
            if(!SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                log.error("发送物流信息失败");
                //TODO持久化到数据库
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @PostConstruct
    public void initData() {
        jdbcTemplate.update("delete from account_tbl");
        jdbcTemplate.update("delete from order_tbl");
        jdbcTemplate.update("delete from storage_tbl");
        jdbcTemplate.update("insert into account_tbl(user_id,money) values('U100000','100000000') ");
        jdbcTemplate.update("insert into storage_tbl(commodity_code,count) values('C100000','20000000') ");
    }

    public boolean validData() {
        Map accountMap = jdbcTemplate.queryForMap("select * from account_tbl where user_id='U100000'");
        if (Integer.parseInt(accountMap.get("money").toString()) < 0) {
            return false;
        }
        Map storageMap = jdbcTemplate.queryForMap("select * from storage_tbl where commodity_code='C100000'");
        if (Integer.parseInt(storageMap.get("count").toString()) < 0) {
            return false;
        }
        return true;
    }
}
