package org.linwg.lib.process;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;

import org.linwg.lib.PerRelation;
import org.linwg.lib.annotation.LUIPermission;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
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
            String[] per = annotation.value();
            List<String> grantStrategy = new ArrayList<>();
            try {
                annotation.grantStrategy();
            } catch (MirroredTypesException e) {
                List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
                if (typeMirrors.size() > 0) {
                    for (TypeMirror typeMirror : typeMirrors) {
                        grantStrategy.add(typeMirror.toString());
                    }
                }
            }
            if (per.length == 0 && grantStrategy.size() == 0) {
                error(field, "Filed %s request permission or grant strategy list be assignment.", field.getSimpleName());
            }

            PerRelation value = annotation.relation();
            ProxyInfo.FieldInfo item = new ProxyInfo.FieldInfo();
            item.per = per;
            item.value = value;
            item.grantStrategy = grantStrategy;
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
                proxyInfos.collectGrantClass();
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
                for (String path : proxyInfos.grantPathList) {
                    sb.append("import ");
                    sb.append(path);
                    sb.append(";\n");
                }
                sb.append("\n");
                sb.append("import org.linwg.lib.PerRelation;\n\n");
                sb.append("import android.widget.Toast;\n");
                sb.append("import org.linwg.lib.api.IPermissionProxy;\n");
                sb.append("import org.linwg.lib.api.InterceptOnClickListener;\n");
                sb.append("import org.linwg.lib.api.UIPermissions;\n\n");
                sb.append("import org.linwg.lib.IPerGrant;\n\n");
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
                sb.append("\tprivate List<String> bak = new ArrayList<>();\n");
                sb.append("\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void subscribe(");
                sb.append(proxyInfos.activityName);
                sb.append(" t){\n");
                sb.append("\t\tthis.host = t;\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tthis.");
                    sb.append(field.fieldName);
                    sb.append(" = t.");
                    sb.append(field.fieldName);
                    sb.append(";\n");
                }
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tinitView(");
                    sb.append(field.fieldName);
                    sb.append(",");
                    sb.append(field.fieldName);
                    sb.append("PerList,");
                    sb.append("PerRelation.");
                    if (field.value == PerRelation.AND) {
                        sb.append("AND,");
                    } else {
                        sb.append("OR,");
                    }
                    sb.append(field.actingOnClick ? "true," : "false,");
                    sb.append("\"");
                    sb.append(field.toastHint);
                    sb.append("\"");
                    if (field.grantStrategy.size() > 0) {
                        for (String path : field.grantStrategy) {
                            sb.append(",");
                            sb.append(classPathToClassName(path));
                            sb.append(".class");
                        }
                    }
                    sb.append(");\n");
                }
                sb.append("\t\tSet<String> allPer = new HashSet<>();\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    sb.append("\t\tallPer.addAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                }
                sb.append("\t\tUIPermissions.addPermissionInstance(this, new ArrayList<String>(allPer));\n");
                if (proxyInfos.grantClassList.size() > 0) {
                    sb.append("\t\tUIPermissions.addPerGrantInstance(this");
                    for (String name : proxyInfos.grantClassList) {
                        sb.append(", ");
                        sb.append(name);
                        sb.append(".class");
                    }
                    sb.append(");\n");
                }
                sb.append("\t}\n\n");

                sb.append("\tprivate void initView(View view, List<String> viewPerList, PerRelation relation, boolean actingOnClick, String actingHint, Class<? extends IPerGrant>... classes) {\n");
                sb.append("\t\tif (view == null) {\n");
                sb.append("\t\t\treturn;\n");
                sb.append("\t\t}\n");
                sb.append("\t\tif (actingOnClick) {\n");
                sb.append("\t\t\tUIPermissions.actingOnClick(view, viewPerList, relation, actingHint, classes);\n");
                sb.append("\t\t} else {\n");
                sb.append("\t\t\tboolean perGrant;\n");
                sb.append("\t\t\tif (classes == null || classes.length == 0) {\n");
                sb.append("\t\t\t\tperGrant = UIPermissions.permissionPrivilege(viewPerList, false);\n");
                sb.append("\t\t\t} else {\n");
                sb.append("\t\t\t\tperGrant = UIPermissions.perGrant(viewPerList, relation, classes);\n");
                sb.append("\t\t\t}\n");
                sb.append("\t\t\tview.setVisibility(perGrant ? View.VISIBLE : View.GONE);\n");
                sb.append("\t\t}\n");
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void onPermissionAdd(List<String> per) {\n");
                sb.append("\t\tbak.clear();\n");
                sb.append("\t\tbak.addAll(per);\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    if (field.actingOnClick || field.grantStrategy.size() > 0) {
                        continue;
                    }
                    sb.append("\t\tif (this.");
                    sb.append(field.fieldName);
                    sb.append(" != null && this.");
                    sb.append(field.fieldName);
                    sb.append(".getVisibility() != View.VISIBLE) {\n");
                    sb.append("\t\t\tbak.retainAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                    sb.append("\t\t\tif (bak.size() > 0) {\n");
                    sb.append("\t\t\t\tinitView(");
                    sb.append(field.fieldName);
                    sb.append(", ");
                    sb.append(field.fieldName);
                    sb.append("PerList, ");
                    sb.append("PerRelation.");
                    if (field.value == PerRelation.AND) {
                        sb.append("AND, ");
                    } else {
                        sb.append("OR, ");
                    }
                    sb.append("false, ");
                    sb.append("\"");
                    sb.append(field.toastHint);
                    sb.append("\"");
                    sb.append(");\n");
                    sb.append("\t\t\t}\n");
                    sb.append("\t\t\tbak.clear();\n");
                    sb.append("\t\t\tbak.addAll(per);\n");
                    sb.append("\t\t}\n");
                }
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void onPermissionRemove(List<String> per) {\n");
                sb.append("\t\tbak.clear();\n");
                sb.append("\t\tbak.addAll(per);\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    if (field.actingOnClick || field.grantStrategy.size() > 0) {
                        continue;
                    }
                    sb.append("\t\tif (this.");
                    sb.append(field.fieldName);
                    sb.append(" != null && this.");
                    sb.append(field.fieldName);
                    sb.append(".getVisibility() == View.VISIBLE) {\n");
                    sb.append("\t\t\tbak.retainAll(");
                    sb.append(field.fieldName);
                    sb.append("PerList);\n");
                    sb.append("\t\t\tif (bak.size() > 0) {\n");
                    sb.append("\t\t\t\tinitView(");
                    sb.append(field.fieldName);
                    sb.append(", ");
                    sb.append(field.fieldName);
                    sb.append("PerList, ");
                    sb.append("PerRelation.");
                    if (field.value == PerRelation.AND) {
                        sb.append("AND, ");
                    } else {
                        sb.append("OR, ");
                    }
                    sb.append("false, ");
                    sb.append("\"");
                    sb.append(field.toastHint);
                    sb.append("\"");
                    sb.append(");\n");
                    sb.append("\t\t\t}\n");
                    sb.append("\t\t\tbak.clear();\n");
                    sb.append("\t\t\tbak.addAll(per);\n");
                    sb.append("\t\t}\n");
                }
                sb.append("\t}\n\n");

                sb.append("\t@Override\n");
                sb.append("\tpublic void onGrantConditionChange() {\n");
                for (ProxyInfo.FieldInfo field : proxyInfos.fieldInfoList) {
                    if (field.actingOnClick || field.grantStrategy.size() == 0) {
                        continue;
                    }
                    sb.append("\t\tif (this.");
                    sb.append(field.fieldName);
                    sb.append(" != null) {\n");
                    sb.append("\t\t\tinitView(");
                    sb.append(field.fieldName);
                    sb.append(", ");
                    sb.append(field.fieldName);
                    sb.append("PerList, ");
                    sb.append("PerRelation.");
                    if (field.value == PerRelation.AND) {
                        sb.append("AND, ");
                    } else {
                        sb.append("OR, ");
                    }
                    sb.append("false, ");
                    sb.append("\"");
                    sb.append(field.toastHint);
                    sb.append("\"");
                    if (field.grantStrategy.size() > 0) {
                        for (String path : field.grantStrategy) {
                            sb.append(", ");
                            sb.append(classPathToClassName(path));
                            sb.append(".class");
                        }
                    }
                    sb.append(");\n");
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

    private String classPathToClassName(String path) {
        int index = path.lastIndexOf(".");
        return index == -1 ? path : path.substring(index + 1);
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

    private void worm(Element element, String message, Object... args) {
        if (args == null) {
            return;
        }
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
    }
}
