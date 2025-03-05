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
package net.linlan.doc.builder;

import net.linlan.doc.factory.BuildTemplateFactory;
import net.linlan.doc.model.ApiConfig;
import net.linlan.doc.model.ApiDoc;
import net.linlan.doc.template.IDocBuildTemplate;
import com.thoughtworks.qdox.JavaProjectBuilder;
import net.linlan.doc.constants.DocGlobalConstants;

import java.util.List;

/**
 * Use to create Asciidoc
 *
 * @author yu 2019/9/26.
 */
public class AdocDocBuilder {

    private static final String API_EXTENSION = "Api.adoc";

    private static final String INDEX_DOC = "index.adoc";

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
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config,false);
        config.setParamsDataToTree(false);
        config.setAdoc(true);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<ApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        if (config.isAllInOne()) {
            String docName = builderTemplate.allInOneDocName(config,INDEX_DOC,".adoc");
            apiDocList = docBuildTemplate.handleApiGroup(apiDocList, config);
            builderTemplate.buildAllInOne(apiDocList, config, javaProjectBuilder, DocGlobalConstants.ALL_IN_ONE_ADOC_TPL, docName);
        } else {
            builderTemplate.buildApiDoc(apiDocList, config, DocGlobalConstants.API_DOC_ADOC_TPL, API_EXTENSION);
            builderTemplate.buildErrorCodeDoc(config, DocGlobalConstants.ERROR_CODE_LIST_ADOC_TPL, DocGlobalConstants.ERROR_CODE_LIST_ADOC);
            builderTemplate.buildDirectoryDataDoc(config, javaProjectBuilder, DocGlobalConstants.DICT_LIST_ADOC_TPL, DocGlobalConstants.DICT_LIST_ADOC);
        }
    }

    /**
     * Generate a single controller api document
     *
     * @param config         ApiConfig
     * @param controllerName controller name
     */
    public static void buildSingleApiDoc(ApiConfig config, String controllerName) {
        config.setAdoc(false);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, new JavaProjectBuilder());
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config,false);
        builderTemplate.buildSingleApi(configBuilder, controllerName, DocGlobalConstants.API_DOC_ADOC_TPL, API_EXTENSION);
    }
}
