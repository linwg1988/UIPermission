package org.linwg.lib;

import java.util.List;

/**
 * 如果权限code无法满足UI权限的判断，额外使用此接口的实现类进行判断
 *
 * @author adr
 */
public interface IPerGrant {
    /**
     * 使用额外的逻辑告知系统是否满足权限
     * @param perList 该控件的权限
     * @param relation 权限之间的关系
     * @return 权限是否通过
     */
    boolean perGrant(List<String> perList ,PerRelation relation);
}
