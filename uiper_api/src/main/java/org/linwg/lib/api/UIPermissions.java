package org.linwg.lib.api;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UIPermissions {
    private static final List<String> mPermissionList = new ArrayList<>();
    private static final Map<String, Set<String>> classMap = new HashMap<>();
    private static final Map<String, List<IPermissionProxy>> instanceMap = new HashMap<>();
    private static final ArrayList<String> returnPer = new ArrayList<>();


    public static void subscribe(Object o) {
        String name = o.getClass().getName();
        String proxyName = name + "$$IPermissionProxy";
        try {
            Class<?> aClass = Class.forName(proxyName);
            IPermissionProxy proxy = (IPermissionProxy) aClass.newInstance();
            proxy.subscribe(o);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void unsubscribe(Object o) {
        String name = o.getClass().getName();
        String proxyName = name + "$$IPermissionProxy";
        List<IPermissionProxy> list = instanceMap.get(proxyName);
        if (list != null) {
            for (IPermissionProxy proxy : list) {
                if (proxy.getHost() == o) {
                    proxy.release();
                    list.remove(proxy);
                    return;
                }
            }
        }
    }

    public static List<String> getPermissionList() {
        returnPer.clear();
        returnPer.addAll(mPermissionList);
        return returnPer;
    }

    public static void setPermissionList(List<String> permissionList) {
        ArrayList<String> temp = new ArrayList<>(mPermissionList);
        mPermissionList.clear();
        if (permissionList != null && permissionList.size() > 0) {
            mPermissionList.addAll(permissionList);
            //增加的权限
            permissionList.removeAll(temp);
            notifyPermissionAdd(permissionList);
        }
        //减少的权限
        temp.removeAll(mPermissionList);
        notifyPermissionRemove(temp);
    }

    private static void notifyPermissionAdd(List<String> permissionList) {
        for (String per : permissionList) {
            Set<String> list = classMap.get(per);
            if (list != null) {
                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()) {
                    String reference = iterator.next();
                    final List<IPermissionProxy> permissionProxies = instanceMap.get(reference);
                    if (permissionProxies != null) {
                        for (IPermissionProxy proxy : permissionProxies) {
                            proxy.onPermissionAdd(permissionList);
                        }
                    }
                }
            }
        }
    }

    private static void notifyPermissionRemove(List<String> permissionList) {
        for (String per : permissionList) {
            Set<String> list = classMap.get(per);
            if (list != null) {
                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()) {
                    String reference = iterator.next();
                    final List<IPermissionProxy> permissionProxies = instanceMap.get(reference);
                    if (permissionProxies != null) {
                        for (IPermissionProxy proxy : permissionProxies) {
                            proxy.onPermissionRemove(permissionList);
                        }
                    }
                }
            }
        }
    }


    public static void addPermissionInstance(@NonNull IPermissionProxy<?> instance, @NonNull List<String> permission) {
        Class<? extends IPermissionProxy> aClass = instance.getClass();
        List<IPermissionProxy> iPermissionProxies = instanceMap.get(aClass.getName());
        if (iPermissionProxies == null) {
            iPermissionProxies = new ArrayList<>();
            instanceMap.put(aClass.getName(), iPermissionProxies);
        }
        iPermissionProxies.add(instance);
        for (String s : permission) {
            Set<String> set = classMap.get(s);
            if (set == null) {
                set = new HashSet<>();
                classMap.put(s, set);
            }
            set.add(aClass.getName());
        }
    }

    public static boolean permissionPrivilege(List<String> permissionList, boolean isOr) {
        ArrayList<String> bakList = new ArrayList<>(permissionList);
        bakList.retainAll(getPermissionList());
        int retainSize = bakList.size();
        if(isOr){
            return retainSize > 0;
        }else{
            return retainSize == permissionList.size();
        }
    }

    public static String getConfigResource(String key) {
        return "没有权限";
    }
}
