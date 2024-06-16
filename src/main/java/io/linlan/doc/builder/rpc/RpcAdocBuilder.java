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

import io.linlan.doc.constants.FrameworkEnum;
import io.linlan.doc.factory.BuildTemplateFactory;
import io.linlan.doc.model.ApiConfig;
import io.linlan.doc.model.rpc.RpcApiDoc;
import com.thoughtworks.qdox.JavaProjectBuilder;
import io.linlan.doc.builder.ProjectDocConfigBuilder;
import io.linlan.doc.constants.DocGlobalConstants;
import io.linlan.doc.template.IDocBuildTemplate;

import java.util.List;

import static io.linlan.doc.constants.DocGlobalConstants.*;

/**
 * @author yu 2020/5/17.
 */
public class RpcAdocBuilder {

    private static final String API_EXTENSION = "RpcApi.adoc";

    private static final String INDEX_DOC = "rpc-index.adoc";

    /**
     * build adoc
     *
     * @param config ApiConfig
     */
    public static void buildApiDoc(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        buildApiDoc(config, javaProjectBuilder);
    }

    /**
     * Only for doc-generate maven plugin and gradle plugin.
     *
     * @param config             ApiConfig
     * @param javaProjectBuilder ProjectDocConfigBuilder
     */
    public static void buildApiDoc(ApiConfig config, JavaProjectBuilder javaProjectBuilder) {
        config.setFramework(FrameworkEnum.DUBBO.getFramework());
        config.setAdoc(true);
        config.setShowJavaType(true);
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInit(config);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<RpcApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        if (config.isAllInOne()) {
            String docName = builderTemplate.allInOneDocName(config, INDEX_DOC, ".adoc");
            builderTemplate.buildAllInOne(apiDocList, config, javaProjectBuilder, DocGlobalConstants.RPC_ALL_IN_ONE_ADOC_TPL, docName);
        } else {
            builderTemplate.buildApiDoc(apiDocList, config, DocGlobalConstants.RPC_API_DOC_ADOC_TPL, API_EXTENSION);
            builderTemplate.buildErrorCodeDoc(config, DocGlobalConstants.ERROR_CODE_LIST_ADOC_TPL, DocGlobalConstants.ERROR_CODE_LIST_ADOC);
        }
    }

}
