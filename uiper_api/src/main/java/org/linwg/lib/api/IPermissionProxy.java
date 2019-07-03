package org.linwg.lib.api;

import java.util.List;

/**
 * @author adr
 */
public interface IPermissionProxy<T> {

    void subscribe(T activity);

    void onPermissionRemove(List<String> permissionList);

    void onPermissionAdd(List<String> permissionList);

    T getHost();

    void release();
}
