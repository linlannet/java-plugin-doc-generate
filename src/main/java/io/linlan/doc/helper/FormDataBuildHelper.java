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

import io.linlan.doc.common.util.RandomUtil;
import io.linlan.doc.utils.StringUtils;
import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.constants.DocGlobalConstants;
import io.linlan.doc.constants.DocTags;
import io.linlan.doc.model.ApiConfig;
import io.linlan.doc.model.CustomField;
import io.linlan.doc.model.DocJavaField;
import io.linlan.doc.model.FormData;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import io.linlan.doc.utils.*;

import java.util.*;

/**
 * @author yu 2019/12/25.
 */
public class FormDataBuildHelper {

    /**
     * build form data
     *
     * @param className       class name
     * @param registryClasses Class container
     * @param counter         invoked counter
     * @param builder         ProjectDocConfigBuilder
     * @param pre             pre
     * @return list of FormData
     */
    public static List<FormData> getFormData(String className, Map<String, String> registryClasses, int counter, ProjectDocConfigBuilder builder, String pre) {
        if (StringUtils.isEmpty(className)) {
            throw new RuntimeException("Class name can't be null or empty.");
        }
        ApiConfig apiConfig = builder.getApiConfig();
        List<FormData> formDataList = new ArrayList<>();
        if (counter > apiConfig.getRecursionLimit()) {
            return formDataList;
        }
        // Check circular reference
        if (registryClasses.containsKey(className) && counter > registryClasses.size()) {
            return formDataList;
        }
        // Registry class
        registryClasses.put(className, className);
        counter++;
        boolean skipTransientField = apiConfig.isSkipTransientField();
        boolean requestFieldToUnderline = apiConfig.isRequestFieldToUnderline();
        boolean responseFieldToUnderline = apiConfig.isResponseFieldToUnderline();
        String simpleName = DocClassUtil.getSimpleName(className);
        String[] globGicName = DocClassUtil.getSimpleGicName(className);
        JavaClass cls = builder.getJavaProjectBuilder().getClassByName(simpleName);
        List<DocJavaField> fields = JavaClassUtil.getFields(cls, 0, new LinkedHashMap<>());

        if (JavaClassValidateUtil.isPrimitive(simpleName)) {
            FormData formData = new FormData();
            formData.setKey(pre);
            formData.setType("text");
            formData.setValue(StringUtilsExt.removeQuotes(RandomUtil.randomValueByType(className)));
            formDataList.add(formData);
            return formDataList;
        }
        if (JavaClassValidateUtil.isCollection(simpleName) || JavaClassValidateUtil.isArray(simpleName)) {
            String gicName = globGicName[0];
            if (JavaClassValidateUtil.isArray(gicName)) {
                gicName = gicName.substring(0, gicName.indexOf("["));
            }
            if (JavaClassValidateUtil.isPrimitive(gicName)) {
                pre = pre.substring(0, pre.lastIndexOf("."));
            }
            formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + "[]"));
        }
        int n = 0;
        out:
        for (DocJavaField docField : fields) {
            JavaField field = docField.getJavaField();
            String fieldName = field.getName();
            String subTypeName = docField.getFullyQualifiedName();
            String fieldGicName = docField.getGenericCanonicalName();
            JavaClass javaClass = field.getType();
            if (field.isStatic() || "this$0".equals(fieldName) ||
                    JavaClassValidateUtil.isIgnoreFieldTypes(subTypeName)) {
                continue;
            }
            if (field.isTransient() && skipTransientField) {
                continue;
            }
            if (responseFieldToUnderline || requestFieldToUnderline) {
                fieldName = StringUtilsExt.camelToUnderline(fieldName);
            }
            Map<String, String> tagsMap = DocUtil.getFieldTagsValue(field, docField);
            if (tagsMap.containsKey(DocTags.IGNORE)) {
                continue out;
            }
            String typeSimpleName = field.getType().getSimpleName();
            if (JavaClassValidateUtil.isMap(subTypeName)) {
                continue;
            }
            String comment = docField.getComment();
            if (StringUtils.isNotEmpty(comment)) {
                comment = DocUtil.replaceNewLineToHtmlBr(comment);
            }
            if (JavaClassValidateUtil.isFile(fieldGicName)) {
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("file");
                formData.setDescription(comment);
                formData.setValue("");
                formDataList.add(formData);
            } else if (JavaClassValidateUtil.isPrimitive(subTypeName)) {
                String fieldValue = "";
                if (tagsMap.containsKey(DocTags.MOCK) && StringUtils.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
                    fieldValue = tagsMap.get(DocTags.MOCK);
                } else {
                    fieldValue = DocUtil.getValByTypeAndFieldName(typeSimpleName, field.getName());
                }
                CustomField customRequestField = builder.getCustomReqFieldMap().get(fieldName);
                // cover request value
                if (Objects.nonNull(customRequestField) && Objects.nonNull(customRequestField.getValue())
                        && JavaClassUtil.isTargetChildClass(simpleName, customRequestField.getOwnerClassName())) {
                    fieldValue = String.valueOf(customRequestField.getValue());
                }
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("text");
                formData.setValue(StringUtilsExt.removeQuotes(fieldValue));
                formData.setDescription(comment);
                formDataList.add(formData);
            } else if (javaClass.isEnum()) {
                Object value = JavaClassUtil.getEnumValue(javaClass, Boolean.TRUE);
                FormData formData = new FormData();
                formData.setKey(pre + fieldName);
                formData.setType("text");
                formData.setValue(StringUtilsExt.removeQuotes(String.valueOf(value)));
                formData.setDescription(comment);
                formDataList.add(formData);
            } else if (JavaClassValidateUtil.isCollection(subTypeName)) {
                String gNameTemp = field.getType().getGenericCanonicalName();
                String[] gNameArr = DocClassUtil.getSimpleGicName(gNameTemp);
                if (gNameArr.length == 0) {
                    continue out;
                }
                String gName = DocClassUtil.getSimpleGicName(gNameTemp)[0];
                if (!JavaClassValidateUtil.isPrimitive(gName)) {
                    if (!simpleName.equals(gName) && !gName.equals(simpleName)) {
                        if (gName.length() == 1) {
                            int len = globGicName.length;
                            if (len > 0) {
                                String gicName = (n < len) ? globGicName[n] : globGicName[len - 1];
                                if (!JavaClassValidateUtil.isPrimitive(gicName) && !simpleName.equals(gicName)) {
                                    formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + fieldName + "[0]."));
                                }
                            }
                        } else {
                            formDataList.addAll(getFormData(gName, registryClasses, counter, builder, pre + fieldName + "[0]."));
                        }
                    }
                }
            } else if (subTypeName.length() == 1 || DocGlobalConstants.JAVA_OBJECT_FULLY.equals(subTypeName)) {
                //  For Generics,do nothing, spring mvc not support
//                if (n < globGicName.length) {
//                    String gicName = globGicName[n];
//                    formDataList.addAll(getFormData(gicName, registryClasses, counter, builder, pre + fieldName + "."));
//                }
//                n++;
                continue;
            } else {
                formDataList.addAll(getFormData(fieldGicName, registryClasses, counter, builder, pre + fieldName + "."));
            }
        }
        return formDataList;
    }
}
