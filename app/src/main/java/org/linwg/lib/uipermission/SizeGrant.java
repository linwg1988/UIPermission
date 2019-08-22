package org.linwg.lib.uipermission;

import org.linwg.lib.IPerGrant;
import org.linwg.lib.PerRelation;
import org.linwg.lib.api.UIPermissions;

import java.util.List;

public class SizeGrant implements IPerGrant {
    @Override
    public boolean perGrant(List<String> perList, PerRelation relation) {
        return UIPermissions.getPermissionList().size() == 4;
    }
}