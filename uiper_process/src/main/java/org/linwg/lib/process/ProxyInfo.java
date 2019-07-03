package org.linwg.lib.process;

import org.linwg.lib.PerRelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author adr
 */
public class ProxyInfo {
    public String packageName;
    public String activityName;
    public String activityFullName;
    public List<FieldInfo> fieldInfoList = new ArrayList<>();

    public ProxyInfo() {
    }

    @Override
    public String toString() {
        return "ProxyInfo{" +
                "packageName='" + packageName + '\'' +
                ", activityName='" + activityName + '\'' +
                ", activityFullName='" + activityFullName + '\'' +
                ", fieldInfoList=" + fieldInfoList +
                '}';
    }


    public static class FieldInfo {
        public String[] per;
        public PerRelation value;
        public String className;
        public String simpleClassName;
        public String fieldName;
        public String upCaseFieldName;
        public boolean actingOnClick;
        public String toastHint;

        @Override
        public String toString() {
            return "FieldInfo{" +
                    "per=" + Arrays.toString(per) +
                    ", value=" + value +
                    ", className='" + className + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    '}';
        }
    }
}
