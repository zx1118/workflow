package com.epiroc.workflow.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.epiroc.workflow.common.entity.WfDictItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface WfDictItemMapper extends BaseMapper<WfDictItem> {

    /**
     * 通过字典id查询字典项
     * @param mainId 字典id
     * @return
     */
    @Select("SELECT * FROM wf_dict_item WHERE DICT_ID = #{mainId} order by sort_order asc, item_value asc")
    public List<WfDictItem> selectItemsByMainId(String mainId);
}
