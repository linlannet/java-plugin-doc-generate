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
package net.linlan.doc.handler;

import net.linlan.doc.builder.ProjectDocConfigBuilder;
import net.linlan.doc.constants.DocAnnotationConstants;
import net.linlan.doc.constants.DocTags;
import net.linlan.doc.constants.SpringMvcAnnotations;
import net.linlan.doc.model.ApiReqParam;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import net.linlan.doc.utils.DocClassUtil;
import net.linlan.doc.utils.DocUtil;
import net.linlan.doc.utils.StringUtilsExt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yu 2019/12/22.
 */
public class SpringMVCRequestHeaderHandler {

    /**
     * handle Spring MVC Request Header
     *
     * @param method         JavaMethod
     * @param projectBuilder projectBuilder
     * @return list of ApiReqHeader
     */
    public List<ApiReqParam> handle(JavaMethod method, ProjectDocConfigBuilder projectBuilder) {
        Map<String, String> constantsMap = projectBuilder.getConstantsMap();
        List<ApiReqParam> mappingHeaders = new ArrayList<>();
        List<JavaAnnotation> annotations = method.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            String annotationName = annotation.getType().getValue();
            Object headersObject = annotation.getNamedParameter("headers");
            if (!isMapping(annotationName) || Objects.isNull(headersObject)) {
                continue;
            }
            String mappingHeader = StringUtilsExt.removeQuotes(headersObject.toString());
            if (!mappingHeader.startsWith("[")) {
                processMappingHeaders(mappingHeader, mappingHeaders);
                continue;
            }
            List<String> headers = (LinkedList) headersObject;
            for (String str : headers) {
                String header = StringUtilsExt.removeQuotes(str);

                if (header.startsWith("!")) {
                    continue;
                }
                processMappingHeaders(header, mappingHeaders);
            }
        }
        List<ApiReqParam> reqHeaders = new ArrayList<>();
        for (JavaParameter javaParameter : method.getParameters()) {
            List<JavaAnnotation> javaAnnotations = javaParameter.getAnnotations();
            String className = method.getDeclaringClass().getCanonicalName();
            Map<String, String> paramMap = DocUtil.getParamsComments(method, DocTags.PARAM, className);
            String paramName = javaParameter.getName();
            ApiReqParam apiReqHeader;
            for (JavaAnnotation annotation : javaAnnotations) {
                String annotationName = annotation.getType().getValue();
                if (SpringMvcAnnotations.REQUEST_HERDER.equals(annotationName)) {
                    apiReqHeader = new ApiReqParam();
                    Map<String, Object> requestHeaderMap = annotation.getNamedParameterMap();
                    if (requestHeaderMap.get(DocAnnotationConstants.VALUE_PROP) != null) {
                        String attribute = DocUtil.handleRequestHeaderValue(annotation);
                        String constValue = ((String) requestHeaderMap.get(DocAnnotationConstants.VALUE_PROP)).replaceAll("\"", "");
                        if (StringUtilsExt.isEmpty(attribute)) {
                            apiReqHeader.setName(constValue);
                        } else {
                            Object value = constantsMap.get(attribute);
                            if (value == null) {
                                apiReqHeader.setName(constValue);
                            } else {
                                apiReqHeader.setName((String) value);
                            }
                        }
                    } else {
                        apiReqHeader.setName(paramName);
                    }
                    StringBuilder desc = new StringBuilder();
                    String comments = paramMap.get(paramName);
                    desc.append(comments);

                    if (requestHeaderMap.get(DocAnnotationConstants.DEFAULT_VALUE_PROP) != null) {
                        apiReqHeader.setValue(StringUtilsExt.removeQuotes((String) requestHeaderMap.get(DocAnnotationConstants.DEFAULT_VALUE_PROP)));
                        desc.append("(defaultValue: ")
                                .append(StringUtilsExt.removeQuotes((String) requestHeaderMap.get(DocAnnotationConstants.DEFAULT_VALUE_PROP)))
                                .append(")");
                    }
                    apiReqHeader.setDesc(desc.toString());
                    if (requestHeaderMap.get(DocAnnotationConstants.REQUIRED_PROP) != null) {
                        apiReqHeader.setRequired(!Boolean.FALSE.toString()
                                .equals(requestHeaderMap.get(DocAnnotationConstants.REQUIRED_PROP)));
                    } else {
                        apiReqHeader.setRequired(true);
                    }
                    String typeName = javaParameter.getType().getValue().toLowerCase();
                    apiReqHeader.setType(DocClassUtil.processTypeNameForParams(typeName));
                    reqHeaders.add(apiReqHeader);
                    break;
                }
            }
        }
        List<ApiReqParam> allApiReqHeaders = Stream.of(mappingHeaders, reqHeaders)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        return allApiReqHeaders;
    }

    private boolean isMapping(String annotationName) {
        switch (annotationName) {
            case "GetMapping":
            case "RequestMapping":
            case "PostMapping":
            case "PutMapping":
            case "PatchMapping":
            case "DeleteMapping":
                return true;
            default:
                return false;
        }
    }

    public void processMappingHeaders(String header, List<ApiReqParam> mappingHeaders) {
        if (header.contains("!=")) {
            String headerName = header.substring(0, header.indexOf("!"));
            ApiReqParam apiReqHeader = ApiReqParam.builder()
                    .setName(headerName)
                    .setRequired(true)
                    .setValue(null)
                    .setDesc("header condition")
                    .setType("string");
            mappingHeaders.add(apiReqHeader);
        } else {
            String headerName;
            String headerValue = null;
            if (header.contains("=")) {
                int index = header.indexOf("=");
                headerName = header.substring(0, index);
                headerValue = header.substring(index + 1);
            } else {
                headerName = header;
            }
            ApiReqParam apiReqHeader = ApiReqParam.builder()
                    .setName(headerName)
                    .setRequired(true)
                    .setValue(headerValue)
                    .setDesc("header condition")
                    .setType("string");
            mappingHeaders.add(apiReqHeader);
        }
    }
}
