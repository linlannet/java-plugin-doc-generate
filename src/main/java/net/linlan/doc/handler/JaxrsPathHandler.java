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

import net.linlan.doc.utils.StringUtils;
import net.linlan.doc.common.util.UrlUtil;
import net.linlan.doc.builder.ProjectDocConfigBuilder;
import net.linlan.doc.model.request.JaxrsPathMapping;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaMethod;
import net.linlan.doc.constants.DocAnnotationConstants;
import net.linlan.doc.constants.DocGlobalConstants;
import net.linlan.doc.constants.DocTags;
import net.linlan.doc.constants.JAXRSAnnotations;
import net.linlan.doc.utils.DocUrlUtil;
import net.linlan.doc.utils.DocUtil;
import net.linlan.doc.utils.StringUtilsExt;

import java.util.*;

/**
 * dubbo Rest 注解处理器
 *
 * @author Zxq
 */
public class JaxrsPathHandler {
    /**
     * ANNOTATION_NAMES
     */
    private static final Set<String> ANNOTATION_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(JAXRSAnnotations.DELETE,
                    DocGlobalConstants.JAXB_DELETE_FULLY,
                    JAXRSAnnotations.PUT, DocGlobalConstants.JAX_PUT_FULLY,
                    JAXRSAnnotations.GET, DocGlobalConstants.JAX_GET_FULLY,
                    JAXRSAnnotations.POST, DocGlobalConstants.JAX_POST_FULLY
            )));

    Map<String, String> constantsMap;

    public JaxrsPathMapping handle(ProjectDocConfigBuilder projectBuilder, String baseUrl, JavaMethod method) {

        List<JavaAnnotation> annotations = method.getAnnotations();
        this.constantsMap = projectBuilder.getConstantsMap();
        String url;
        String methodType = null;
        String shortUrl = null;
        String mediaType = null;
        String serverUrl = projectBuilder.getServerUrl();
        String contextPath = projectBuilder.getApiConfig().getPathPrefix();
        boolean deprecated = false;
        for (JavaAnnotation annotation : annotations) {
            String annotationName = annotation.getType().getName();
            if (JAXRSAnnotations.JAX_PRODUCES.equals(annotationName)) {
                mediaType = DocUtil.getRequestHeaderValue(annotation);
            }
            // Deprecated annotation on method
            if (DocAnnotationConstants.DEPRECATED.equals(annotationName)) {
                deprecated = true;
            }
            if (JAXRSAnnotations.JAX_PATH.equals(annotationName) ||
                    JAXRSAnnotations.JAX_PATH_PARAM.equals(annotationName) ||
                    DocGlobalConstants.JAX_PATH_FULLY
                            .equals(annotationName)) {
                shortUrl = DocUtil.handleMappingValue(annotation);
            }
            if (ANNOTATION_NAMES.contains(annotationName)) {
                methodType = annotationName;
            }
        }
        // @deprecated tag on method
        if (Objects.nonNull(method.getTagByName(DocTags.DEPRECATED))) {
            deprecated = true;
        }
        JaxrsPathMapping jaxrsPathMapping = getJaxbPathMapping(projectBuilder, baseUrl, method, shortUrl, serverUrl, contextPath);
        if (jaxrsPathMapping != null) {
            return jaxrsPathMapping.setDeprecated(deprecated)
                    .setMethodType(methodType)
                    .setMediaType(mediaType);
        }
        return null;
    }

    private JaxrsPathMapping getJaxbPathMapping(ProjectDocConfigBuilder projectBuilder,
                                                String baseUrl, JavaMethod method,
                                                String shortUrl,
                                                String serverUrl,
                                                String contextPath) {
        String url;
        if (Objects.nonNull(shortUrl)) {
            if (Objects.nonNull(method.getTagByName(DocTags.IGNORE))) {
                return null;
            }
            shortUrl = StringUtilsExt.removeQuotes(shortUrl);
            List<String> urls = DocUtil.split(shortUrl);
            url = String.join(DocGlobalConstants.PATH_DELIMITER, serverUrl, contextPath, baseUrl, shortUrl);
            shortUrl = String.join(DocGlobalConstants.PATH_DELIMITER, DocGlobalConstants.PATH_DELIMITER, contextPath, baseUrl, shortUrl);
            if (urls.size() > 1) {
                url = DocUrlUtil.getMvcUrls(serverUrl, contextPath + "/" + baseUrl, urls);
                shortUrl = DocUrlUtil.getMvcUrls(DocGlobalConstants.EMPTY, contextPath + "/" + baseUrl, urls);
            }
            for (Map.Entry<String, String> entry : constantsMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (url.contains(key)) {
                    url = url.replace(key, value).replace("+", "");
                }
                if (shortUrl.contains(key)) {
                    shortUrl = shortUrl.replace(key, value).replace("+", "");
                }
            }
            String urlSuffix = projectBuilder.getApiConfig().getUrlSuffix();
            url = UrlUtil.simplifyUrl(url);
            shortUrl = UrlUtil.simplifyUrl(shortUrl);
            if (StringUtils.isNotEmpty(urlSuffix)) {
                url += urlSuffix;
                shortUrl += urlSuffix;
            }
            return JaxrsPathMapping.builder()
                    .setUrl(StringUtils.trim(url))
                    .setShortUrl(StringUtils.trim(shortUrl));
        }
        return null;
    }

}
