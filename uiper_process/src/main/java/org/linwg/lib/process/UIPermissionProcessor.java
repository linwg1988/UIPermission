package org.linwg.lib.process;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;

import org.linwg.lib.PerRelation;
import org.linwg.lib.annotation.LUIPermission;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class UIPermissionProcessor extends AbstractProcessor {

    private String PROXY = "IPermissionProxy";
    private Filer mFileUtils;
    private Elements mElementUtils;
    private Messager mMessager;
    Map<String, ProxyInfo> map = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFileUtils = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<String>();
        annotationTypes.add(LUIPermission.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        map.clear();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(LUIPermission.class);

        for (Element element : elements) {
            if (!(element instanceof VariableElement)) {
                continue;
            }
            if (!checkAnnotationValid(element, LUIPermission.class)) {
                break;
            }


            VariableElement field = (VariableElement) element;
            Element enclosingElement = field.getEnclosingElement();
            TypeElement activity = (TypeElement) enclosingElement;
            String activityFullName = activity.getQualifiedName().toString();
            String activityName = activity.getSimpleName().toString();
            ProxyInfo proxyInfo = map.get(activityFullName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo();
                map.put(activityFullName, proxyInfo);
            }
            proxyInfo.packageName = activityFullName.replace("." + activityName, "");
            proxyInfo.activityName = activityName;
            proxyInfo.activityFullName = activityFullName;

            String simpleName = field.getSimpleName().toString();
            LUIPermission annotation = field.getAnnotation(LUIPermission.class);
            String[] per = annotation.per();
            if (per.length == 0) {
                error(field, "Filed %s request permission list be assignment.", field.getSimpleName());
            }
            PerRelation value = annotation.relation();
            ProxyInfo.FieldInfo item = new ProxyInfo.FieldInfo();
            item.per = per;
            item.value = value;
            item.className = field.asType().toString();
            item.simpleClassName = item.className.replace(item.className.substring(0, item.className.lastIndexOf(".") + 1), "");
            item.fieldName = simpleName;
            item.actingOnClick = annotation.actingOnClick();
            item.toastHint = annotation.toastHint();
            String firstLetter = item.fieldName.substring(0, 1);
            item.upCaseFieldName = item.fieldName.replaceFirst(firstLetter, firstLetter.toUpperCase());
            proxyInfo.fieldInfoList.add(item);
        }


        if (map.size() > 0) {
            Set<String> strings = map.keySet();
            for (String key : strings) {
                ProxyInfo proxyInfos = map.get(key);
                StringBuilder sb = new StringBuilder();
                sb.append("package ");
                sb.append(proxyInfos.packageName);
                sb.append(";\n\n");
                sb.append("import android.view.View;\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("import ");
                    sb.append(field.className);
                    sb.append(";\n");
                }
                sb.append("\n");
                sb.append("import org.linwg.lib.PerRelation;\n\n");
                sb.append("import android.widget.Toast;\n");
                sb.append("import org.linwg.lib.api.IPermissionProxy;\n");
                sb.append("import org.linwg.lib.api.InterceptOnClickListener;\n");
                sb.append("import org.linwg.lib.api.UIPermissions;\n\n");
                sb.append("import java.lang.reflect.Field;\n");
                sb.append("import java.util.ArrayList;\n");
                sb.append("import java.util.Arrays;\n");
                sb.append("import java.util.List;\n");
                sb.append("import java.util.Set;\n");
                sb.append("import java.util.HashSet;\n\n");
                sb.append("public class ");
                sb.append(proxyInfos.activityName);
                sb.append("$$IPermissionProxy");
                sb.append(" implements ");
                sb.append(PROXY);
                sb.append("<");
                sb.append(proxyInfos.activityName);
                sb.append(">");
                sb.append(" {\n\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\tprivate ");
                    sb.append(field.simpleClassName);
                    sb.append(" ");
                    sb.append(field.fieldName);
                    sb.append(";\n");
                    sb.append("\tprivate List<String> ");
                    sb.append(field.fieldName);
                    sb.append("PerList = Arrays.asList(");
                    for (int i = 0; i < field.per.length; i++) {
                        sb.append("\"");
                        sb.append(field.per[i]);
                        sb.append("\"");
                        if (i < field.per.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(");\n");
                }
                sb.append("\tprivate ");
                sb.append(proxyInfos.activityName);
                sb.append(" host;\n");
                sb.append("\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void subscribe(");
                sb.append(proxyInfos.activityName);
                sb.append(" t){\n");
                sb.append("\t\tthis.host = t;\n");
                sb.append("\t\tList<String> permissionList = UIPermissions.getPermissionList();\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tthis.");
                    sb.append(field.fieldName);
                    sb.append(" = t.");
                    sb.append(field.fieldName);
                    sb.append(";\n");
                }
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tinit");
                    sb.append(field.upCaseFieldName);
                    sb.append("(permissionList);\n");
                }
                sb.append("\t\tSet<String> allPer = new HashSet<>();\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tallPer.addAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                }
                sb.append("\t\tUIPermissions.addPermissionInstance(this, new ArrayList<String>(allPer));\n");
                sb.append("\t}\n\n");

                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\tprivate void init");
                    sb.append(field.upCaseFieldName);
                    sb.append("(List<String> permissionList) {\n");

                    sb.append("\t\tif(this.");
                    sb.append(field.fieldName);
                    sb.append(" != null){\n");
//                    sb.append("\t\t\tArrayList<String> bakList = new ArrayList<>(");
//                    sb.append(field.fieldName);
//                    sb.append("PerList);\n");
//                    sb.append("\t\t\tbakList.retainAll(permissionList);\n");
//                    sb.append("\t\t\tint retainSize = bakList.size();\n");

                    sb.append("\t\t\tthis.");
                    sb.append(field.fieldName);
                    sb.append(".setTag(org.linwg.lib.api.R.id.ui_per_list, ");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                    sb.append("\t\t\tthis.");
                    sb.append(field.fieldName);
                    sb.append(".setTag(org.linwg.lib.api.R.id.ui_per_relation, ");
                    sb.append("PerRelation.");
                    if (field.value == PerRelation.AND) {
                        sb.append("AND);\n");
                    } else {
                        sb.append("OR);\n");
                    }

                    if (field.actingOnClick) {
                        sb.append("\t\t\ttry {\n");
                        sb.append("\t\t\t\tClass<? extends View> aClass = View.class;\n");
                        sb.append("\t\t\t\tField mListenerInfo = aClass.getDeclaredField(\"mListenerInfo\");\n");
                        sb.append("\t\t\t\tmListenerInfo.setAccessible(true);\n");
                        sb.append("\t\t\t\tObject o = mListenerInfo.get(this.");
                        sb.append(field.fieldName);
                        sb.append(");\n");
                        sb.append("\t\t\t\tif(o == null){\n");
                        sb.append("\t\t\t\t\treturn;\n");
                        sb.append("\t\t\t\t}\n");
                        sb.append("\t\t\t\tClass<?> lisClazz = o.getClass();\n");
                        sb.append("\t\t\t\tField mOnClickListener = lisClazz.getField(\"mOnClickListener\");\n");
                        sb.append("\t\t\t\tObject lis = mOnClickListener.get(o);\n");
                        sb.append("\t\t\t\tif(lis != null){\n");
                        sb.append("\t\t\t\t\tmOnClickListener.setAccessible(true);\n");
                        sb.append("\t\t\t\t\tmOnClickListener.set(o, new InterceptOnClickListener((View.OnClickListener) lis) {\n");
                        sb.append("\t\t\t\t\t\t@Override\n");
                        sb.append("\t\t\t\t\t\tpublic void onClick(View v) {\n");
                        sb.append("\t\t\t\t\t\t\tif (!UIPermissions.permissionPrivilege(");
                        sb.append(field.fieldName);
                        sb.append("PerList, ");
                        sb.append(field.value == PerRelation.AND ? "false" : "true");
                        sb.append(")) {\n");
                        sb.append("\t\t\t\t\t\t\t\tToast.makeText(host, ");
                        if (field.toastHint == null || field.toastHint.equals("")) {
                            sb.append("UIPermissions.getConfigResource(\"default_hint\")");
                        } else {
                            sb.append("\"");
                            sb.append(field.toastHint);
                            sb.append("\"");
                        }
                        sb.append(", Toast.LENGTH_SHORT).show();\n");
                        sb.append("\t\t\t\t\t\t\t\treturn;\n");
                        sb.append("\t\t\t\t\t\t\t}\n");
                        sb.append("\t\t\t\t\t\t\tthis.target.onClick(v);\n");
                        sb.append("\t\t\t\t\t\t}\n");
                        sb.append("\t\t\t\t\t});\n");
                        sb.append("\t\t\t\t}\n");
                        sb.append("\t\t\t} catch (NoSuchFieldException e) {\n");
                        sb.append("\t\t\t\t// do nothing\n");
                        sb.append("\t\t\t} catch (IllegalAccessException e) {\n");
                        sb.append("\t\t\t\t// do nothing\n");
                        sb.append("\t\t\t}\n");
                    } else {
                        if (field.value == PerRelation.AND) {
                            sb.append("\t\t\tif(retainSize == ");
                            sb.append(field.fieldName);
                            sb.append("PerList.size()){\n");
                            sb.append("\t\t\t\tthis.");
                            sb.append(field.fieldName);
                            sb.append(".setVisibility(View.VISIBLE);\n");
                            sb.append("\t\t\t}else{\n");
                            sb.append("\t\t\t\tthis.");
                            sb.append(field.fieldName);
                            sb.append(".setVisibility(View.GONE);\n");
                            sb.append("\t\t\t}");
                        } else {
                            sb.append("\t\t\tif(retainSize > 0){\n");
                            sb.append("\t\t\t\tthis.");
                            sb.append(field.fieldName);
                            sb.append(".setVisibility(View.VISIBLE);\n");
                            sb.append("\t\t\t}else{\n");
                            sb.append("\t\t\t\tthis.");
                            sb.append(field.fieldName);
                            sb.append(".setVisibility(View.GONE);\n");
                            sb.append("\t\t\t}");
                        }
                    }
                    sb.append("\n");


                    sb.append("\t\t}\n");
                    sb.append("\t}\n\n");
                }

                sb.append("\t@Override\n");
                sb.append("\tpublic void onPermissionAdd(List<String> per) {\n");
                sb.append("\t\tList<String> permissionList = UIPermissions.getPermissionList();\n");
                sb.append("\t\tArrayList<String> bak = new ArrayList<>(per);\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tif (this.");
                    sb.append(field.fieldName);
                    sb.append(" != null && this.");
                    sb.append(field.fieldName);
                    sb.append(".getVisibility() != View.VISIBLE) {\n");
                    sb.append("\t\t\tbak.retainAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                    sb.append("\t\t\tif (bak.size() > 0) {\n");
                    sb.append("\t\t\t\tinit");
                    sb.append(field.upCaseFieldName);
                    sb.append("(permissionList);\n");
                    sb.append("\t\t\t}\n");
                    sb.append("\t\t\tbak.clear();\n");
                    sb.append("\t\t\tbak.addAll(per);\n");
                    sb.append("\t\t}\n");
                }
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void onPermissionRemove(List<String> per) {\n");
                sb.append("\t\tList<String> permissionList = UIPermissions.getPermissionList();\n");
                sb.append("\t\tArrayList<String> bak = new ArrayList<>(per);\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tif (this.");
                    sb.append(field.fieldName);
                    sb.append(" != null && this.");
                    sb.append(field.fieldName);
                    sb.append(".getVisibility() == View.VISIBLE) {\n");
                    sb.append("\t\t\tbak.retainAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                    sb.append("\t\t\tif (bak.size() > 0) {\n");
                    sb.append("\t\t\t\tinit");
                    sb.append(field.upCaseFieldName);
                    sb.append("(permissionList);\n");
                    sb.append("\t\t\t}\n");
                    sb.append("\t\t\tbak.clear();\n");
                    sb.append("\t\t\tbak.addAll(per);\n");
                    sb.append("\t\t}\n");
                }
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic ");
                sb.append(proxyInfos.activityName);
                sb.append(" getHost() {\n");
                sb.append("\t\treturn host;\n");
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void release() {\n");
                sb.append("\t\tthis.host = null;\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tthis.");
                    sb.append(field.fieldName);
                    sb.append(" = null;\n");
                }
                sb.append("\t}\n");

                sb.append("}\n");

                try {
                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfos.activityFullName + "$$IPermissionProxy",
                            (Element) null);
                    Writer writer = jfo.openWriter();
                    writer.write(sb.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    error(null,
                            "Unable to write injector for type %s: %s",
                            null, e.getMessage());
                }
            }

        }
        return true;
    }

    private boolean checkAnnotationValid(Element annotatedElement, Class<LUIPermission> clazz) {
        if (!SuperficialValidation.validateElement(annotatedElement)) {
            return false;
        }
        if (annotatedElement.getKind() != ElementKind.FIELD) {
            error(annotatedElement, "%s must be declared on class.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args == null) {
            return;
        }
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
