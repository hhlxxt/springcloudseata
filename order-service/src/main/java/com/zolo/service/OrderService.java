package com.zolo.service;

import com.zolo.entity.CommonDto;
import com.zolo.feign.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


@Service
public class OrderService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int create(CommonDto dto) {

        int orderMoney = dto.getOrderCount() * 100;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert order_tbl(user_id,commodity_code,count,money,wms_status,order_No) values(?,?,?,?,?,?)" ;
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, dto.getUserId());
                ps.setString(2, dto.getCommodityCode());
                ps.setInt(3, dto.getOrderCount());
                ps.setInt(4, orderMoney);
                ps.setString(5,"0");
                ps.setString(6,dto.getOrderNo());
                return ps;
            }
        },keyHolder);
        /*jdbcTemplate.update("insert order_tbl(user_id,commodity_code,count,money) values(?,?,?,?)",
            new Object[] {userId, commodityCode, count, orderMoney});*/

        userFeignClient.reduce(dto.getUserId(), orderMoney);

        return keyHolder.getKey().intValue();


    }

    public int updateOrderStatusById(String orderNO, String wms_status) {
       return jdbcTemplate.update("update order_tbl set wms_status= ? where order_no = ? ",new Object[]{wms_status,orderNO});
    }
}
