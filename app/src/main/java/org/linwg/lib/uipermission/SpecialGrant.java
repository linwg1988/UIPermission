package org.linwg.lib.uipermission;

import org.linwg.lib.IPerGrant;
import org.linwg.lib.PerRelation;
import org.linwg.lib.api.UIPermissions;

import java.util.List;

public class SpecialGrant implements IPerGrant {
    @Override
    public boolean perGrant(List<String> perList, PerRelation relation) {
        List<String> permissionList = UIPermissions.getPermissionList();
        return permissionList.contains("A") && permissionList.contains("C") && !permissionList.contains("B");
    }
}