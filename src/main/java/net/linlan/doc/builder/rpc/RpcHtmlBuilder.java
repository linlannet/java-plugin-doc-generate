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
package net.linlan.doc.builder.rpc;

import net.linlan.doc.common.util.FileUtil;
import net.linlan.doc.builder.ProjectDocConfigBuilder;
import net.linlan.doc.factory.BuildTemplateFactory;
import net.linlan.doc.model.ApiConfig;
import net.linlan.doc.model.rpc.RpcApiDoc;
import net.linlan.doc.template.IDocBuildTemplate;
import net.linlan.doc.utils.BeetlTemplateUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import net.linlan.doc.constants.DocGlobalConstants;
import org.beetl.core.Template;

import java.util.List;

/**
 * @author yu 2020/5/17.
 */
public class RpcHtmlBuilder {

    private static long now = System.currentTimeMillis();

    private static String INDEX_HTML = "rpc-index.html";

    private static String SEARCH_JS = "search.js";


    /**
     * build controller api
     *
     * @param config config
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
        config.setShowJavaType(true);
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInit(config);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<RpcApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        Template indexCssTemplate = BeetlTemplateUtil.getByName(DocGlobalConstants.ALL_IN_ONE_CSS);
        FileUtil.nioWriteFile(indexCssTemplate.render(), config.getOutPath() + DocGlobalConstants.FILE_SEPARATOR + DocGlobalConstants.ALL_IN_ONE_CSS_OUT);
        builderTemplate.copyJarFile("css/" + DocGlobalConstants.FONT_STYLE, config.getOutPath() + DocGlobalConstants.FILE_SEPARATOR + DocGlobalConstants.FONT_STYLE);
        builderTemplate.copyJarFile("js/" + DocGlobalConstants.JQUERY, config.getOutPath() + DocGlobalConstants.FILE_SEPARATOR + DocGlobalConstants.JQUERY);
        builderTemplate.buildAllInOne(apiDocList, config, javaProjectBuilder, DocGlobalConstants.RPC_ALL_IN_ONE_HTML_TPL, INDEX_HTML);
        builderTemplate.buildSearchJs(apiDocList, config, DocGlobalConstants.RPC_ALL_IN_ONE_SEARCH_TPL, SEARCH_JS);
    }
}
