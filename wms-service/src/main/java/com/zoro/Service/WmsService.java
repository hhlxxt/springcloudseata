package com.zoro.Service;

import com.zoro.persistence.Wms;
import com.zoro.persistence.WmsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class WmsService {

    @Autowired
    private WmsMapper wmsMapper ;

    @Transactional
    public int createWms(Wms wms){

        int result = wmsMapper.createWms(wms);
        if (result == 0){
            throw new RuntimeException("创建物流信息失败");
        }
        return result ;
    }

    public Wms selectWmsByOrderNo(String orderNo){
        return wmsMapper.selectWmsByOrderNo(orderNo) ;
    }
}
