package com.epiroc.workflow.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epiroc.workflow.common.entity.param.OperateParam;
import com.epiroc.workflow.common.mapper.WfProcessMapper;
import com.epiroc.workflow.common.entity.WfProcess;
import com.epiroc.workflow.common.service.WfProcessService;
import com.epiroc.workflow.common.system.query.QueryWrapperBuilder;
import com.epiroc.workflow.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WfProcessServiceImpl extends ServiceImpl<WfProcessMapper, WfProcess>
        implements WfProcessService {

    @Resource
    private WfProcessMapper wfProcessMapper;

    /**
     * 根据条件查询一条记录
     * @param wfProcess
     * @return
     */
    @Override
    public WfProcess queryWfProcessOne(WfProcess wfProcess) {
        if (wfProcess != null) {
            QueryWrapper<WfProcess> wrapper = QueryWrapperBuilder.buildQueryWrapper(wfProcess);
            return wfProcessMapper.selectOne(wrapper);
        }
        return null;
    }

    @Override
    public WfProcess getWfProcessById(Integer wfProcessId) {
        return wfProcessMapper.selectById(wfProcessId);
    }

    @Override
    public WfProcess getWfProcessByOperateParam(OperateParam operateParam) {
        WfProcess wfProcess = null;
        if(oConvertUtils.isEmpty(operateParam.getWfProcessId())){
            wfProcess = queryWfProcessOne(oConvertUtils.entityToModel(operateParam, WfProcess.class));
        } else {
            wfProcess = getWfProcessById(operateParam.getWfProcessId());
        }
        return wfProcess;
    }

}
