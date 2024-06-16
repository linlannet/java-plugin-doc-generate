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
package io.linlan.doc.handler;

import io.linlan.doc.utils.StringUtilsExt;
import io.linlan.doc.utils.StringUtils;
import io.linlan.doc.common.util.UrlUtil;
import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.constants.DocAnnotationConstants;
import io.linlan.doc.constants.DocGlobalConstants;
import io.linlan.doc.constants.Methods;
import io.linlan.doc.constants.SpringMvcAnnotations;
import io.linlan.doc.model.request.RequestMapping;
import io.linlan.doc.utils.DocUrlUtil;
import io.linlan.doc.utils.DocUtil;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaMethod;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.linlan.doc.constants.DocTags.DEPRECATED;
import static io.linlan.doc.constants.DocTags.IGNORE;

/**
 * @author yu 2019/12/22.
 */
public class SpringMVCRequestMappingHandler {

    /**
     * handle spring request mapping
     *
     * @param projectBuilder    projectBuilder
     * @param controllerBaseUrl spring mvc controller base url
     * @param method            JavaMethod
     * @param constantsMap      project constant container
     * @return RequestMapping
     */
    public RequestMapping handle(ProjectDocConfigBuilder projectBuilder, String controllerBaseUrl, JavaMethod method, Map<String, String> constantsMap) {
        List<JavaAnnotation> annotations = method.getAnnotations();
        String url;
        String methodType = null;
        String shortUrl = null;
        String mediaType = null;
        String serverUrl = projectBuilder.getServerUrl();
        String contextPath = projectBuilder.getApiConfig().getPathPrefix();
        boolean deprecated = false;
        for (JavaAnnotation annotation : annotations) {
            String annotationName = annotation.getType().getName();
            Object produces = annotation.getNamedParameter("produces");
            if (Objects.nonNull(produces)) {
                mediaType = produces.toString();
            }
            if (DocAnnotationConstants.DEPRECATED.equals(annotationName)) {
                deprecated = true;
            }
            if (SpringMvcAnnotations.REQUEST_MAPPING.equals(annotationName) || DocGlobalConstants.REQUEST_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                Object nameParam = annotation.getNamedParameter("method");
                if (Objects.nonNull(nameParam)) {
                    methodType = nameParam.toString();
                    methodType = DocUtil.handleHttpMethod(methodType);
                } else {
                    methodType = Methods.GET.getValue();
                }
            } else if (SpringMvcAnnotations.GET_MAPPING.equals(annotationName) || DocGlobalConstants.GET_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                methodType = Methods.GET.getValue();
            } else if (SpringMvcAnnotations.POST_MAPPING.equals(annotationName) || DocGlobalConstants.POST_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                methodType = Methods.POST.getValue();
            } else if (SpringMvcAnnotations.PUT_MAPPING.equals(annotationName) || DocGlobalConstants.PUT_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                methodType = Methods.PUT.getValue();
            } else if (SpringMvcAnnotations.PATCH_MAPPING.equals(annotationName) || DocGlobalConstants.PATCH_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                methodType = Methods.PATCH.getValue();
            } else if (SpringMvcAnnotations.DELETE_MAPPING.equals(annotationName) || DocGlobalConstants.DELETE_MAPPING_FULLY.equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
                methodType = Methods.DELETE.getValue();
            }
        }
        if (Objects.nonNull(method.getTagByName(DEPRECATED))) {
            deprecated = true;
        }
        if (Objects.nonNull(shortUrl)) {
            if (Objects.nonNull(method.getTagByName(IGNORE))) {
                return null;
            }
            shortUrl = StringUtilsExt.removeQuotes(shortUrl);
            List<String> urls = DocUtil.split(shortUrl);
            if (urls.size() > 1) {
                url = DocUrlUtil.getMvcUrls(serverUrl, contextPath + "/" + controllerBaseUrl, urls);
                shortUrl = DocUrlUtil.getMvcUrls(DocGlobalConstants.EMPTY, contextPath + "/" + controllerBaseUrl, urls);
            } else {
                url = String.join(DocGlobalConstants.PATH_DELIMITER, serverUrl, contextPath, controllerBaseUrl, shortUrl);
                shortUrl = String.join(DocGlobalConstants.PATH_DELIMITER, DocGlobalConstants.PATH_DELIMITER, contextPath, controllerBaseUrl, shortUrl);
            }
            for (Map.Entry<String, String> entry : constantsMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                url = delConstantsUrl(url, key, value);
                shortUrl = delConstantsUrl(shortUrl, key, value);
            }
            String urlSuffix = projectBuilder.getApiConfig().getUrlSuffix();
            if (StringUtils.isNotEmpty(urlSuffix)) {
                url = UrlUtil.simplifyUrl(StringUtils.trim(url)) + urlSuffix;
                shortUrl = UrlUtil.simplifyUrl(StringUtils.trim(shortUrl)) + urlSuffix;
            } else {
                url = UrlUtil.simplifyUrl(StringUtils.trim(url));
                shortUrl = UrlUtil.simplifyUrl(StringUtils.trim(shortUrl));
            }
            return RequestMapping.builder().setMediaType(mediaType).setMethodType(methodType)
                    .setUrl(url).setShortUrl(shortUrl).setDeprecated(deprecated);
        }
        return null;
    }

    public static String delConstantsUrl(String url, String replaceKey, String replaceValue) {
        url = StringUtils.trim(url);
        url = url.replace("+", "");
        url = UrlUtil.simplifyUrl(url);
        String[] pathWords = url.split("/");
        for (String word : pathWords) {
            if (word.equals(replaceKey)) {
                url = url.replace(replaceKey, replaceValue);
                return url;
            }
        }
        return url;
    }
}
