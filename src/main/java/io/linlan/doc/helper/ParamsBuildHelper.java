/**
 * Copyright 2018-2023 the original author or Linlan authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.linlan.doc.helper;

import io.linlan.doc.common.model.EnumDictionary;
import io.linlan.doc.common.util.CollectionUtil;
import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.constants.DocAnnotationConstants;
import io.linlan.doc.constants.DocGlobalConstants;
import io.linlan.doc.constants.DocTags;
import io.linlan.doc.constants.ValidatorAnnotations;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import io.linlan.doc.utils.StringUtils;
import io.linlan.doc.model.*;
import io.linlan.doc.utils.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yu 2019/12/21.
 */
public class ParamsBuildHelper {

    public static List<ApiParam> buildParams(String className, String pre, int level, String isRequired, boolean isResp,
                                             Map<String, String> registryClasses, ProjectDocConfigBuilder projectBuilder,
                                             List<String> groupClasses, int pid, boolean jsonRequest) {

        Map<String, String> genericMap = new HashMap<>(10);

        if (StringUtils.isEmpty(className)) {
            throw new RuntimeException("Class name can't be null or empty.");
        }
        ApiConfig apiConfig = projectBuilder.getApiConfig();
        int nextLevel = level + 1;
        // Check circular reference
        List<ApiParam> paramList = new ArrayList<>();
        if (level > apiConfig.getRecursionLimit()) {
            return paramList;
        }
        if (registryClasses.containsKey(className) && level > registryClasses.size()) {
            return paramList;
        }
        Map<String, CustomField> responseFieldMap = projectBuilder.getCustomRespFieldMap();
        boolean skipTransientField = apiConfig.isSkipTransientField();
        boolean isShowJavaType = projectBuilder.getApiConfig().getShowJavaType();
        boolean requestFieldToUnderline = projectBuilder.getApiConfig().isRequestFieldToUnderline();
        boolean responseFieldToUnderline = projectBuilder.getApiConfig().isResponseFieldToUnderline();
        boolean displayActualType = projectBuilder.getApiConfig().isDisplayActualType();
        // Registry class
        registryClasses.put(className, className);
        String simpleName = DocClassUtil.getSimpleName(className);
        String[] globGicName = DocClassUtil.getSimpleGicName(className);
        JavaClass cls = projectBuilder.getClassByName(simpleName);
        if (Objects.isNull(globGicName) || globGicName.length < 1) {
            // obtain generics from parent class
            JavaClass superJavaClass = cls != null ? cls.getSuperJavaClass() : null;
            if (superJavaClass != null && !"Object".equals(superJavaClass.getSimpleName())) {
                globGicName = DocClassUtil.getSimpleGicName(superJavaClass.getGenericFullyQualifiedName());
            }
        }

        JavaClassUtil.genericParamMap(genericMap, cls, globGicName);
        List<DocJavaField> fields = JavaClassUtil.getFields(cls, 0, new LinkedHashMap<>());
        if (JavaClassValidateUtil.isPrimitive(simpleName)) {
            String processedType = isShowJavaType ? simpleName : DocClassUtil.processTypeNameForParams(simpleName.toLowerCase());
            paramList.addAll(primitiveReturnRespComment(processedType));
        } else if (JavaClassValidateUtil.isCollection(simpleName) || JavaClassValidateUtil.isArray(simpleName)) {
            if (!JavaClassValidateUtil.isCollection(globGicName[0])) {
                String gicName = globGicName[0];
                if (JavaClassValidateUtil.isArray(gicName)) {
                    gicName = gicName.substring(0, gicName.indexOf("["));
                }
                paramList.addAll(buildParams(gicName, pre, nextLevel, isRequired, isResp,
                        registryClasses, projectBuilder, groupClasses, pid, jsonRequest));
            }
        } else if (JavaClassValidateUtil.isMap(simpleName)) {
            if (globGicName.length == 2) {
                paramList.addAll(buildParams(globGicName[1], pre, nextLevel, isRequired, isResp,
                        registryClasses, projectBuilder, groupClasses, pid, jsonRequest));
            }
        } else if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(className)) {
            ApiParam param = ApiParam.of().setId(pid + 1).setField(pre + "any object").setType("object").setPid(pid);
            if (StringUtils.isEmpty(isRequired)) {
                param.setDesc(DocGlobalConstants.ANY_OBJECT_MSG).setVersion(DocGlobalConstants.DEFAULT_VERSION);
            } else {
                param.setDesc(DocGlobalConstants.ANY_OBJECT_MSG).setRequired(false).setVersion(DocGlobalConstants.DEFAULT_VERSION);
            }
            paramList.add(param);
        } else if (JavaClassValidateUtil.isReactor(simpleName)) {
            if (globGicName.length > 0) {
                paramList.addAll(buildParams(globGicName[0], pre, nextLevel, isRequired, isResp,
                        registryClasses, projectBuilder, groupClasses, pid, jsonRequest));
            }
        } else {
            Map<String, String> ignoreFields = JavaClassUtil.getClassJsonIgnoreFields(cls);
            out:
            for (DocJavaField docField : fields) {
                String maxLength = null;
                JavaField field = docField.getJavaField();
                if (field.isTransient() && skipTransientField) {
                    continue;
                }
                String fieldName = docField.getFieldName();
                if (ignoreFields.containsKey(fieldName)) {
                    continue;
                }

                String subTypeName = docField.getFullyQualifiedName();
                if ((responseFieldToUnderline && isResp) || (requestFieldToUnderline && !isResp)) {
                    fieldName = StringUtilsExt.camelToUnderline(fieldName);
                }
                String typeSimpleName = field.getType().getSimpleName();
                String fieldGicName = docField.getGenericCanonicalName();
                List<JavaAnnotation> javaAnnotations = docField.getAnnotations();

                Map<String, String> tagsMap = DocUtil.getFieldTagsValue(field, docField);
                String since = DocGlobalConstants.DEFAULT_VERSION;//since tag value
                if (!isResp) {
                    pre:
                    if (tagsMap.containsKey(DocTags.IGNORE)) {
                        continue out;
                    } else if (tagsMap.containsKey(DocTags.SINCE)) {
                        since = tagsMap.get(DocTags.SINCE);
                    }
                } else {
                    if (tagsMap.containsKey(DocTags.SINCE)) {
                        since = tagsMap.get(DocTags.SINCE);
                    }
                }
                boolean strRequired = false;
                int annotationCounter = 0;
                CustomField customResponseField = responseFieldMap.get(simpleName + "." + fieldName);
                if (customResponseField != null && JavaClassUtil.isTargetChildClass(simpleName, customResponseField.getOwnerClassName())
                        && (customResponseField.isIgnore()) && isResp) {
                    continue;
                }
                CustomField customRequestField = projectBuilder.getCustomReqFieldMap().get(simpleName + "." + fieldName);
                if (customRequestField != null && JavaClassUtil.isTargetChildClass(simpleName, customRequestField.getOwnerClassName())
                        && (customRequestField.isIgnore()) && !isResp) {
                    continue;
                }
                an:
                for (JavaAnnotation annotation : javaAnnotations) {
                    String simpleAnnotationName = annotation.getType().getValue();
                    AnnotationValue annotationValue = null;
                    if (DocAnnotationConstants.MAX.equalsIgnoreCase(simpleAnnotationName)) {
                        annotationValue = annotation.getProperty(DocAnnotationConstants.VALUE_PROP);
                    }
                    if (DocAnnotationConstants.SIZE.equalsIgnoreCase(simpleAnnotationName)) {
                        annotationValue = annotation.getProperty(DocAnnotationConstants.MAX);
                    }
                    if (DocAnnotationConstants.LENGTH.equalsIgnoreCase(simpleAnnotationName)) {
                        annotationValue = annotation.getProperty(DocAnnotationConstants.MAX);
                    }
                    if (!Objects.isNull(annotationValue)) {
                        maxLength = annotationValue.toString();
                    }

                    if (DocAnnotationConstants.JSON_PROPERTY.equalsIgnoreCase(simpleAnnotationName)) {
                        AnnotationValue value = annotation.getProperty("access");
                        if (Objects.nonNull(value)) {
                            if (DocGlobalConstants.JSON_PROPERTY_READ_ONLY.equals(value.getParameterValue()) && !isResp) {
                                continue out;
                            }
                            if (DocGlobalConstants.JSON_PROPERTY_WRITE_ONLY.equals(value.getParameterValue()) && isResp) {
                                continue out;
                            }
                        }
                    }
                    if (DocAnnotationConstants.SHORT_JSON_IGNORE.equals(simpleAnnotationName)) {
                        continue out;
                    } else if (DocAnnotationConstants.SHORT_JSON_FIELD.equals(simpleAnnotationName)) {
                        if (null != annotation.getProperty(DocAnnotationConstants.SERIALIZE_PROP)) {
                            if (Boolean.FALSE.toString().equals(annotation.getProperty(DocAnnotationConstants.SERIALIZE_PROP).toString())) {
                                continue out;
                            }
                        } else if (null != annotation.getProperty(DocAnnotationConstants.NAME_PROP)) {
                            fieldName = StringUtilsExt.removeQuotes(annotation.getProperty(DocAnnotationConstants.NAME_PROP).toString());
                        }
                    } else if (DocAnnotationConstants.SHORT_JSON_PROPERTY.equals(simpleAnnotationName)) {
                        if (null != annotation.getProperty(DocAnnotationConstants.VALUE_PROP)) {
                            fieldName = StringUtilsExt.removeQuotes(annotation.getProperty(DocAnnotationConstants.VALUE_PROP).toString());
                        }
                    } else if (ValidatorAnnotations.NULL.equals(simpleAnnotationName) && !isResp) {
                        List<String> groupClassList = JavaClassUtil.getParamGroupJavaClass(annotation);
                        for (String javaClass : groupClassList) {
                            if (groupClasses.contains(javaClass)) {
                                strRequired = false;
                                continue out;
                            }
                        }
                    } else if (JavaClassValidateUtil.isJSR303Required(simpleAnnotationName) && !isResp) {
                        annotationCounter++;
                        boolean hasGroup = false;
                        List<String> groupClassList = JavaClassUtil.getParamGroupJavaClass(annotation);
                        for (String javaClass : groupClassList) {
                            if (groupClasses.contains(javaClass)) {
                                hasGroup = true;
                            }
                        }
                        if (hasGroup) {
                            strRequired = true;
                        } else if (CollectionUtil.isEmpty(groupClasses)) {
                            strRequired = true;
                        }
                        break an;
                    }
                }
                String fieldValue = "";
                if (tagsMap.containsKey(DocTags.MOCK) && StringUtils.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                    fieldValue = tagsMap.get(DocTags.MOCK);
                    if (!DocUtil.javaPrimaryType(typeSimpleName)
                            && !JavaClassValidateUtil.isCollection(subTypeName)
                            && !JavaClassValidateUtil.isMap(subTypeName)
                            && !JavaClassValidateUtil.isArray(subTypeName)) {
                        fieldValue = DocUtil.handleJsonStr(fieldValue);
                    }
                }
                if (annotationCounter < 1) {
                    doc:
                    if (tagsMap.containsKey(DocTags.REQUIRED)) {
                        strRequired = true;
                        break doc;
                    }
                }
                // cover response value
                if (Objects.nonNull(customResponseField) && isResp && Objects.nonNull(customResponseField.getValue())
                        && JavaClassUtil.isTargetChildClass(simpleName, customResponseField.getOwnerClassName())) {
                    fieldValue = String.valueOf(customResponseField.getValue());
                }
                // cover request value
                if (Objects.nonNull(customRequestField) && !isResp && Objects.nonNull(customRequestField.getValue())
                        && JavaClassUtil.isTargetChildClass(simpleName, customRequestField.getOwnerClassName())) {
                    fieldValue = String.valueOf(customRequestField.getValue());
                }
                //cover required
                if (customRequestField != null && !isResp && JavaClassUtil.isTargetChildClass(simpleName, customRequestField.getOwnerClassName())
                        && customRequestField.isRequire()) {
                    strRequired = true;
                }
                //cover comment
                String comment = "";
                if (null != customRequestField && StringUtils.isNotEmpty(customRequestField.getDesc())
                        && JavaClassUtil.isTargetChildClass(simpleName, customRequestField.getOwnerClassName()) && !isResp) {
                    comment = customRequestField.getDesc();
                }
                if (null != customResponseField && StringUtils.isNotEmpty(customResponseField.getDesc())
                        && JavaClassUtil.isTargetChildClass(simpleName, customResponseField.getOwnerClassName()) && isResp) {
                    comment = customResponseField.getDesc();
                }
                if (StringUtils.isBlank(comment)) {
                    comment = docField.getComment();
                }
                if (StringUtils.isNotEmpty(comment)) {
                    comment = DocUtil.replaceNewLineToHtmlBr(comment);
                }
                // file
                if (JavaClassValidateUtil.isFile(fieldGicName)) {
                    ApiParam param = ApiParam.of().setField(pre + fieldName).setType("file")
                            .setPid(pid).setId(paramList.size() + pid + 1)
                            .setMaxLength(maxLength)
                            .setDesc(comment).setRequired(Boolean.valueOf(isRequired)).setVersion(since);
                    paramList.add(param);
                    continue;
                }
                if (JavaClassValidateUtil.isPrimitive(subTypeName)) {
                    if (StringUtils.isEmpty(fieldValue)) {
                        fieldValue = DocUtil.getValByTypeAndFieldName(typeSimpleName, field.getName());
                    }
                    ApiParam param = ApiParam.of().setField(pre + fieldName);
                    param.setPid(pid).setMaxLength(maxLength).setValue(fieldValue);
                    String processedType = isShowJavaType ? typeSimpleName : DocClassUtil.processTypeNameForParams(typeSimpleName.toLowerCase());
                    param.setType(processedType);
                    if (StringUtils.isNotEmpty(comment)) {
                        commonHandleParam(paramList, param, isRequired, comment, since, strRequired);
                    } else {
                        commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND, since, strRequired);
                    }
                } else {
                    String appendComment = "";
                    if (displayActualType) {
                        if (globGicName.length > 0) {
                            String gicName = genericMap.get(subTypeName) != null ? genericMap.get(subTypeName) : globGicName[0];
                            if (!simpleName.equals(gicName)) {
                                appendComment = " (ActualType: " + JavaClassUtil.getClassSimpleName(gicName) + ")";
                            }
                        }
                        if (Objects.nonNull(docField.getActualJavaType())) {
                            appendComment = " (ActualType: " + JavaClassUtil.getClassSimpleName(docField.getActualJavaType()) + ")";
                        }
                    }

                    StringBuilder preBuilder = new StringBuilder();
                    for (int j = 0; j < level; j++) {
                        preBuilder.append(DocGlobalConstants.FIELD_SPACE);
                    }
                    preBuilder.append("└─");
                    int fieldPid;
                    ApiParam param = ApiParam.of().setField(pre + fieldName).setPid(pid).setMaxLength(maxLength);

                    String processedType;
                    if (typeSimpleName.length() == 1) {
                        processedType = DocClassUtil.processTypeNameForParams(typeSimpleName.toLowerCase());
                    } else {
                        processedType = isShowJavaType ? typeSimpleName : DocClassUtil.processTypeNameForParams(typeSimpleName.toLowerCase());
                    }
                    param.setType(processedType);
                    JavaClass javaClass = field.getType();
                    if (javaClass.isEnum()) {
                        comment = comment + handleEnumComment(javaClass, projectBuilder);
                        param.setType(DocGlobalConstants.ENUM);
                        if (!isResp) {
                            List<JavaMethod> methods = javaClass.getMethods();
                            int index = 0;
                            enumOut:
                            for (JavaMethod method : methods) {
                                List<JavaAnnotation> javaAnnotationList = method.getAnnotations();
                                for (JavaAnnotation annotation : javaAnnotationList) {
                                    if (annotation.getType().getValue().contains("JsonValue")) {
                                        break enumOut;
                                    }
                                }
                                if (CollectionUtil.isEmpty(javaAnnotations) && index < 1) {
                                    break enumOut;
                                }
                                index++;
                            }
                            Object value = JavaClassUtil.getEnumValue(javaClass, !jsonRequest);
                            param.setValue(String.valueOf(value));
                            param.setEnumValues(JavaClassUtil.getEnumValues(javaClass));
                        }
                        if (StringUtils.isNotEmpty(comment)) {
                            commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                        } else {
                            commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                        }

                    } else if (JavaClassValidateUtil.isCollection(subTypeName) || JavaClassValidateUtil.isArray(subTypeName)) {
                        param.setType("array");
                        if (tagsMap.containsKey(DocTags.MOCK) && StringUtils.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                            param.setValue(fieldValue);
                        }

                        if (globGicName.length > 0 && "java.util.List".equals(fieldGicName)) {
                            fieldGicName = fieldGicName + "<T>";
                        }
                        if (JavaClassValidateUtil.isArray(subTypeName)) {
                            fieldGicName = fieldGicName.substring(0, fieldGicName.lastIndexOf("["));
                            fieldGicName = "java.util.List<" + fieldGicName + ">";
                        }
                        String[] gNameArr = DocClassUtil.getSimpleGicName(fieldGicName);
                        if (gNameArr.length == 0) {
                            continue out;
                        }
                        if (gNameArr.length > 0) {
                            String gName = DocClassUtil.getSimpleGicName(fieldGicName)[0];
                            JavaClass javaClass1 = projectBuilder.getJavaProjectBuilder().getClassByName(gName);
                            comment = comment + handleEnumComment(javaClass1, projectBuilder);
                        }
                        String gName = gNameArr[0];
                        if (JavaClassValidateUtil.isPrimitive(gName)) {
                            String builder = DocUtil.jsonValueByType(gName) +
                                    "," +
                                    DocUtil.jsonValueByType(gName);
                            if (StringUtils.isEmpty(fieldValue)) {
                                param.setValue(DocUtil.handleJsonStr(builder));
                            } else {
                                param.setValue(fieldValue);
                            }
                            if (StringUtils.isNotEmpty(comment)) {
                                commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                            } else {
                                commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                            }
                        } else {
                            if (StringUtils.isNotEmpty(comment)) {
                                commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                            } else {
                                commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                            }
                            fieldPid = paramList.size() + pid;
                            if (!simpleName.equals(gName) && !gName.equals(simpleName)) {
                                JavaClass arraySubClass = projectBuilder.getJavaProjectBuilder().getClassByName(gName);
                                if (arraySubClass.isEnum()) {
                                    Object value = JavaClassUtil.getEnumValue(arraySubClass, Boolean.FALSE);
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("[\"").append(value).append("\"]");
                                    param.setValue(sb.toString());
                                } else if (gName.length() == 1) {
                                    // handle generic
                                    int len = globGicName.length;
                                    if (len < 1) {
                                        continue out;
                                    }
                                    String gicName = genericMap.get(gName) != null ? genericMap.get(gName) : globGicName[0];
                                    if (!JavaClassValidateUtil.isPrimitive(gicName) && !simpleName.equals(gicName)) {
                                        paramList.addAll(buildParams(gicName, preBuilder.toString(), nextLevel, isRequired,
                                                isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                    }
                                } else {
                                    paramList.addAll(buildParams(gName, preBuilder.toString(), nextLevel, isRequired,
                                            isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                }
                            }
                        }

                    } else if (JavaClassValidateUtil.isMap(subTypeName)) {
                        if (tagsMap.containsKey(DocTags.MOCK) && StringUtils.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                            param.setType("map");
                            param.setValue(fieldValue);
                        }

                        if (StringUtils.isNotEmpty(comment)) {
                            commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                        } else {
                            commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                        }
                        fieldPid = paramList.size() + pid;
                        String gNameTemp = fieldGicName;
                        String valType = DocClassUtil.getMapKeyValueType(gNameTemp).length == 0 ? gNameTemp : DocClassUtil.getMapKeyValueType(gNameTemp)[1];
                        if (JavaClassValidateUtil.isMap(gNameTemp) || DocGlobalConstants.JAVA_OBJECT_FULLY.equals(valType)) {
                            ApiParam param1 = ApiParam.of().setField(preBuilder.toString() + "any object")
                                    .setId(fieldPid + 1).setPid(fieldPid)
                                    .setMaxLength(maxLength)
                                    .setType("object").setDesc(DocGlobalConstants.ANY_OBJECT_MSG).setVersion(DocGlobalConstants.DEFAULT_VERSION);
                            paramList.add(param1);
                            continue;
                        }
                        if (!JavaClassValidateUtil.isPrimitive(valType)) {
                            if (valType.length() == 1) {
                                String gicName = genericMap.get(valType);
                                if (!JavaClassValidateUtil.isPrimitive(gicName) && !simpleName.equals(gicName)) {
                                    paramList.addAll(buildParams(gicName, preBuilder.toString(), nextLevel, isRequired,
                                            isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                }
                            } else {
                                paramList.addAll(buildParams(valType, preBuilder.toString(), nextLevel, isRequired,
                                        isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                            }
                        }
                    } else if (subTypeName.length() == 1 || DocGlobalConstants.JAVA_OBJECT_FULLY.equals(subTypeName)) {
                        if (StringUtils.isNotEmpty(comment)) {
                            commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                        } else {
                            commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                        }
                        fieldPid = paramList.size() + pid;
                        // handle java generic or object
                        if (DocGlobalConstants.JAVA_OBJECT_FULLY.equals(subTypeName) && StringUtils.isNotEmpty(field.getComment())) {
                            ApiParam param1 = ApiParam.of().setField(preBuilder.toString() + "any object")
                                    .setId(paramList.size())
                                    .setMaxLength(maxLength)
                                    .setType("object").setDesc(DocGlobalConstants.ANY_OBJECT_MSG).setVersion(DocGlobalConstants.DEFAULT_VERSION);
                            paramList.add(param1);
                        } else if (!simpleName.equals(className)) {
                            if (globGicName.length > 0) {
                                String gicName = genericMap.get(subTypeName) != null ? genericMap.get(subTypeName) : globGicName[0];
                                String simple = DocClassUtil.getSimpleName(gicName);
                                if (JavaClassValidateUtil.isPrimitive(simple)) {
                                    //do nothing
                                } else if (gicName.contains("<")) {
                                    if (JavaClassValidateUtil.isCollection(simple)) {
                                        param.setType(DocGlobalConstants.ARRAY);
                                        String gName = DocClassUtil.getSimpleGicName(gicName)[0];
                                        if (!JavaClassValidateUtil.isPrimitive(gName)) {
                                            paramList.addAll(buildParams(gName, preBuilder.toString(), nextLevel, isRequired,
                                                    isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                        }
                                    } else if (JavaClassValidateUtil.isMap(simple)) {
                                        String valType = DocClassUtil.getMapKeyValueType(gicName)[1];
                                        if (!JavaClassValidateUtil.isPrimitive(valType)) {
                                            paramList.addAll(buildParams(valType, preBuilder.toString(), nextLevel, isRequired,
                                                    isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                        }
                                    } else {
                                        paramList.addAll(buildParams(gicName, preBuilder.toString(), nextLevel, isRequired,
                                                isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                    }
                                } else {
                                    paramList.addAll(buildParams(gicName, preBuilder.toString(), nextLevel, isRequired,
                                            isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                                }
                            } else {
                                paramList.addAll(buildParams(subTypeName, preBuilder.toString(), nextLevel, isRequired,
                                        isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));
                            }
                        }
                    } else if (simpleName.equals(subTypeName)) {
                        //do nothing
                    } else {
                        if (StringUtils.isNotEmpty(comment)) {
                            commonHandleParam(paramList, param, isRequired, comment + appendComment, since, strRequired);
                        } else {
                            commonHandleParam(paramList, param, isRequired, DocGlobalConstants.NO_COMMENTS_FOUND + appendComment, since, strRequired);
                        }
                        fieldGicName = DocUtil.formatFieldTypeGicName(genericMap, globGicName, fieldGicName);
                        fieldPid = paramList.size() + pid;
                        paramList.addAll(buildParams(fieldGicName, preBuilder.toString(), nextLevel, isRequired,
                                isResp, registryClasses, projectBuilder, groupClasses, fieldPid, jsonRequest));

                    }
                }
            }//end field
        }
        return paramList;
    }

    public static String dictionaryListComment(ApiDataDictionary dictionary) {
        List<EnumDictionary> enumDataDict = dictionary.getEnumDataDict();
        return enumDataDict.stream().map(apiDataDictionary ->
                apiDataDictionary.getName() + "-(\"" + apiDataDictionary.getValue() + "\",\""
                        + apiDataDictionary.getDesc() + "\")"
        ).collect(Collectors.joining(","));
    }

    public static List<ApiParam> primitiveReturnRespComment(String typeName) {
        StringBuilder comments = new StringBuilder();
        comments.append("Return ").append(typeName).append(".");
        ApiParam apiParam = ApiParam.of().setField("-")
                .setType(typeName).setDesc(comments.toString()).setVersion(DocGlobalConstants.DEFAULT_VERSION);
        List<ApiParam> paramList = new ArrayList<>();
        paramList.add(apiParam);
        return paramList;
    }

    private static void commonHandleParam(List<ApiParam> paramList, ApiParam param, String isRequired,
                                          String comment, String since, boolean strRequired) {
        if (StringUtils.isEmpty(isRequired)) {
            param.setDesc(comment).setVersion(since);
        } else {
            param.setDesc(comment).setVersion(since).setRequired(strRequired);
        }
        param.setId(paramList.size() + param.getPid() + 1);
        paramList.add(param);
    }

    private static String handleEnumComment(JavaClass javaClass, ProjectDocConfigBuilder projectBuilder) {
        String comment = "";
        if (!javaClass.isEnum()) {
            return comment;
        }
        String enumComments = javaClass.getComment();
        if (projectBuilder.getApiConfig().getInlineEnum()) {
            ApiDataDictionary dataDictionary = projectBuilder.getApiConfig().getDataDictionary(javaClass.getCanonicalName());
            if (Objects.isNull(dataDictionary)) {
                comment = comment + "<br/>" + JavaClassUtil.getEnumParams(javaClass);
            } else {
                comment = comment + "[enum:" + dictionaryListComment(dataDictionary) + "]";
            }
        } else {
            enumComments = DocUtil.replaceNewLineToHtmlBr(enumComments);
            if (StringUtils.isNotEmpty(enumComments)) {
                comment = comment + "(See: " + enumComments + ")";
            }
            comment = StringUtilsExt.removeQuotes(comment);
        }
        return comment;
    }
}
