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

import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.model.ApiReqParam;
import io.linlan.doc.utils.DocUtil;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaMethod;
import io.linlan.doc.constants.JAXRSAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Zxq
 */
public class JaxrsHeaderHandler {

    /**
     * Handle JAX RS Header
     * @param method method
     * @param projectBuilder ProjectDocConfigBuilder
     * @return list of ApiReqParam
     */
    public List<ApiReqParam> handle(JavaMethod method, ProjectDocConfigBuilder projectBuilder) {
        List<JavaAnnotation> annotations = method.getAnnotations();
        List<ApiReqParam> ApiReqParams = new ArrayList<>();
        for (JavaAnnotation annotation : annotations) {
            // hit target head annotation
            if (JAXRSAnnotations.JAX_HEADER_PARAM.equals(annotation.getType().getName())) {
                ApiReqParam ApiReqParam = new ApiReqParam();
                // Obtain header value
                ApiReqParam.setValue(DocUtil.getRequestHeaderValue(annotation).replaceAll("\"", ""));
                ApiReqParam.setName(DocUtil.getRequestHeaderValue(annotation).replaceAll("\"", ""));
                ApiReqParam.setType("string");
                ApiReqParam.setDesc("desc");
                ApiReqParams.add(ApiReqParam);
            }
        }
        return ApiReqParams;
    }
}
