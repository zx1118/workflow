package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.enums.StateEnum;
import com.epiroc.workflow.common.service.WfDictLoadService;
import com.epiroc.workflow.common.util.DateUtils;
import com.epiroc.workflow.common.entity.WfOrder;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.mapper.WfOrderMapper;
import com.epiroc.workflow.common.service.WfOrderService;
import com.epiroc.workflow.common.util.OrderNoUtil;
import com.epiroc.workflow.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WfOrderServiceImpl extends ServiceImpl<WfOrderMapper, WfOrder> implements WfOrderService {

    @Resource
    private WfDictLoadService wfDictLoadService;

    @Override
    public WfOrder saveSubmitOrder(WfOrder wfOrder, WfProcess wfProcess) {
        wfOrder.setProcessId(wfProcess.getId());
        wfOrder.setRequestDate(DateUtils.getDate());
        wfOrder.setUnitId(wfProcess.getUnitId());
        save(wfOrder);
        return wfOrder;
    }

    @Override
    public WfOrder getOrder(WfOrder order) {
        String orderStatus = order.getOrderStatus();
        WfOrder wfOrder = new WfOrder();
        // 查询 wf_order
        if(oConvertUtils.isNotEmpty(order.getId())){
            wfOrder = getById(order.getId());
            wfOrder.setUpdateTime(DateUtils.getDate());
        } else {
            wfOrder = oConvertUtils.entityToModel(order, WfOrder.class);
            if(wfDictLoadService.getOrderStatusCacheInfo().containsKey(orderStatus)){
                wfOrder.setOrderStatus(wfDictLoadService.getOrderStatusCacheInfo().get(orderStatus));
            }
            assert wfOrder != null;
            wfOrder.setCreateBy(order.getUpdateBy());
            wfOrder.setCreateTime(DateUtils.getDate());
            save(wfOrder);
        }
        return wfOrder;
    }

    @Override
    public WfOrder getOrderByOperateParam(OperateParam operateParam) {
        WfProcess process = operateParam.getWfProcess();
        String orderStatus = operateParam.getOrderStatus();
        new WfOrder();
        WfOrder wfOrder;
        // 查询 wf_order
        if(oConvertUtils.isNotEmpty(operateParam.getOrderId())){
            wfOrder = getById(operateParam.getOrderId());
            wfOrder.setUpdateBy(operateParam.getOperatorEmail());
            wfOrder.setUpdateTime(DateUtils.getDate());
        } else {
            wfOrder = oConvertUtils.entityToModel(operateParam, WfOrder.class);
            if(wfDictLoadService.getOrderStatusCacheInfo().containsKey(orderStatus)){
                wfOrder.setOrderStatus(wfDictLoadService.getOrderStatusCacheInfo().get(orderStatus));
            }
            wfOrder.setProcessId(process.getId());
            // 创建订单编号
            String orderNo = OrderNoUtil.generateOrderNo(process.getOrderNoPre(), process.getOrderNoLength());
            wfOrder.setOrderNo(orderNo);
            assert wfOrder != null;
            wfOrder.setCreateBy(operateParam.getOperatorEmail());
            wfOrder.setCreateTime(DateUtils.getDate());
            wfOrder.setUpdateBy(operateParam.getOperatorEmail());
            save(wfOrder);
        }
        return wfOrder;
    }

}
