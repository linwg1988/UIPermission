package org.linwg.lib.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import org.linwg.lib.IPerGrant;
import org.linwg.lib.PerRelation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UIPermissions {
    private static final List<String> mPermissionList = new ArrayList<>();
    private static final List<String> bakPerList = new ArrayList<>();
    /**
     * Permission code map proxy class name set.
     */
    private static final Map<String, Set<String>> classMap = new HashMap<>();
    /**
     * IPerGrant class name map proxy class name set.
     */
    private static final Map<String, Set<String>> grantToProxyMap = new HashMap<>();
    /**
     * Proxy class name map proxy instance.
     */
    private static final Map<String, List<IPermissionProxy>> instanceMap = new HashMap<>();
    private static final ArrayList<String> returnPer = new ArrayList<>();
    /**
     * IPerGrant class name map grant instance.
     */
    private static final Map<String, IPerGrant> grantMap = new HashMap<>();
    /**
     * If true when {@link #setPermissionList(List)} is been call will also call all IPerGrant's host
     * to reset UI.
     */
    private static boolean perEffectGrant = true;

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
        if (perEffectGrant) {
            onGrantConditionChange();
        }
    }

    /**
     * This will call all IPerGrant instance active.
     */
    public static void onGrantConditionChange() {
        Set<String> keySet = grantMap.keySet();
        for (String key : keySet) {
            IPerGrant iPerGrant = grantMap.get(key);
            if (iPerGrant != null) {
                onGrantConditionChange(iPerGrant.getClass());
            }
        }
    }

    /**
     * Just call a special IPerGrant instance active.
     */
    public static void onGrantConditionChange(Class<? extends IPerGrant> clazz) {
        String name = clazz.getName();
        Set<String> proxyNames = grantToProxyMap.get(name);
        if (proxyNames == null) {
            return;
        }
        for (String proxyName : proxyNames) {
            List<IPermissionProxy> proxyInstanceList = instanceMap.get(proxyName);
            if (proxyInstanceList == null) {
                return;
            }
            for (IPermissionProxy proxy : proxyInstanceList) {
                proxy.onGrantConditionChange();
            }
        }
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

    public static void addPerGrantInstance(@NonNull IPermissionProxy<?> instance, @Nullable Class<? extends IPerGrant>... classes) {
        if (classes == null || classes.length == 0) {
            return;
        }
        Class<? extends IPermissionProxy> aClass = instance.getClass();
        List<IPermissionProxy> iPermissionProxies = instanceMap.get(aClass.getName());
        if (iPermissionProxies == null) {
            iPermissionProxies = new ArrayList<>();
            instanceMap.put(aClass.getName(), iPermissionProxies);
        }
        iPermissionProxies.add(instance);
        for (Class<? extends IPerGrant> s : classes) {
            Set<String> set = grantToProxyMap.get(s);
            if (set == null) {
                set = new HashSet<>();
                grantToProxyMap.put(s.getName(), set);
            }
            set.add(aClass.getName());
        }
    }

    public static boolean permissionPrivilege(List<String> permissionList, boolean isOr) {
        bakPerList.clear();
        bakPerList.addAll(permissionList);
        bakPerList.retainAll(mPermissionList);
        int retainSize = bakPerList.size();
        if (isOr) {
            return retainSize > 0;
        } else {
            return retainSize == permissionList.size();
        }
    }

    public static String getConfigResource(String key) {
        return "没有权限";
    }

    public static IPerGrant getGrantStrategy(Class<? extends IPerGrant> clazz) {
        String name = clazz.getName();
        if (grantMap.get(name) == null) {
            IPerGrant iPerGrant = null;
            try {
                iPerGrant = clazz.newInstance();
                grantMap.put(name, iPerGrant);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return iPerGrant;
        }
        return grantMap.get(name);
    }

    public static boolean perGrant(List<String> permissionList, PerRelation relation, Class<? extends IPerGrant>... classes) {
        if (relation == PerRelation.AND) {
            for (Class<? extends IPerGrant> clazz : classes) {
                boolean b = getGrantStrategy(clazz).perGrant(permissionList, relation);
                if (!b) {
                    return false;
                }
            }
            return true;
        } else {
            for (Class<? extends IPerGrant> clazz : classes) {
                boolean b = getGrantStrategy(clazz).perGrant(permissionList, relation);
                if (b) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void actingOnClick(final View view, final List<String> viewPerList, final PerRelation relation, final String actingHint, final Class<? extends IPerGrant>[] classes) {
        try {
            Class<? extends View> aClass = View.class;
            Field mListenerInfo = aClass.getDeclaredField("mListenerInfo");
            mListenerInfo.setAccessible(true);
            Object o = mListenerInfo.get(view);
            if (o == null) {
                return;
            }
            Class<?> lisClazz = o.getClass();
            Field mOnClickListener = lisClazz.getField("mOnClickListener");
            Object lis = mOnClickListener.get(o);
            if (lis != null) {
                mOnClickListener.setAccessible(true);
                mOnClickListener.set(o, new InterceptOnClickListener((View.OnClickListener) lis) {
                    @Override
                    public void onClick(View v) {
                        if (classes == null || classes.length == 0) {
                            if (!permissionPrivilege(viewPerList, relation == PerRelation.OR)) {
                                Toast.makeText(view.getContext(), actingHint, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            if (!perGrant(viewPerList, relation, classes)) {
                                Toast.makeText(view.getContext(), actingHint, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        this.target.onClick(v);
                    }
                });
            }
        } catch (NoSuchFieldException e) {
            // do nothing
        } catch (IllegalAccessException e) {
            // do nothing
        }
    }
}
