package com.zoro.persistence;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WmsMapper {

    /**
     * 创建物流信息
     *
     * @param wms
     *          物流对象
     *
     * @return 创建结果
     */
    public int createWms(Wms wms);

    /**
     * 根据订单id获取消息物流信息
     *
     * @param orderId
     * @return
     */
    public Wms selectWmsByOrderId(int orderId);

}
