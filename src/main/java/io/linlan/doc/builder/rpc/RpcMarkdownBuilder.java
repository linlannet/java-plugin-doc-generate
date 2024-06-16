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
package io.linlan.doc.builder.rpc;

import io.linlan.doc.common.util.DateTimeUtil;
import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.factory.BuildTemplateFactory;
import io.linlan.doc.model.ApiConfig;
import io.linlan.doc.model.rpc.RpcApiDoc;
import io.linlan.doc.template.IDocBuildTemplate;
import com.thoughtworks.qdox.JavaProjectBuilder;
import io.linlan.doc.constants.DocGlobalConstants;

import java.util.List;

/**
 * @author yu 2020/5/16.
 */
public class RpcMarkdownBuilder {

    private static final String API_EXTENSION = "Api.md";

    private static final String DATE_FORMAT = "yyyyMMddHHmm";

    /**
     * @param config ApiConfig
     */
    public static void buildApiDoc(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        buildApiDoc(config, javaProjectBuilder);
    }

    /**
     * Only for doc-generate maven plugin and gradle plugin.
     *
     * @param apiConfig          ApiConfig
     * @param javaProjectBuilder ProjectDocConfigBuilder
     */
    public static void buildApiDoc(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        apiConfig.setAdoc(false);
        apiConfig.setShowJavaType(true);
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInit(apiConfig);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(apiConfig, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(apiConfig.getFramework());
        List<RpcApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        if (apiConfig.isAllInOne()) {
            String version = apiConfig.isCoverOld() ? "" : "-V" + DateTimeUtil.long2Str(System.currentTimeMillis(), DATE_FORMAT);
            String docName = builderTemplate.allInOneDocName(apiConfig, "rpc-all" + version, ".md");
            builderTemplate.buildAllInOne(apiDocList, apiConfig, javaProjectBuilder, DocGlobalConstants.RPC_ALL_IN_ONE_MD_TPL, docName);
        } else {
            builderTemplate.buildApiDoc(apiDocList, apiConfig, DocGlobalConstants.RPC_API_DOC_MD_TPL, API_EXTENSION);
            builderTemplate.buildErrorCodeDoc(apiConfig, DocGlobalConstants.ERROR_CODE_LIST_MD_TPL, DocGlobalConstants.ERROR_CODE_LIST_MD);
        }
    }
}
