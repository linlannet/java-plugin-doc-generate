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

import com.google.gson.Gson;
import net.linlan.doc.common.util.CollectionUtil;
import net.linlan.doc.utils.StringUtils;
import net.linlan.doc.builder.ProjectDocConfigBuilder;
import net.linlan.doc.common.util.OkHttp3Util;
import net.linlan.doc.constants.TornaConstants;
import net.linlan.doc.factory.BuildTemplateFactory;
import net.linlan.doc.model.ApiConfig;
import net.linlan.doc.model.rpc.RpcApiDoc;
import net.linlan.doc.model.torna.Apis;
import net.linlan.doc.model.torna.DubboInfo;
import net.linlan.doc.model.torna.TornaApi;
import net.linlan.doc.model.torna.TornaDic;
import net.linlan.doc.template.IDocBuildTemplate;
import net.linlan.doc.utils.DocUtil;
import net.linlan.doc.utils.TornaUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xingzi 2021/4/28 16:14
 **/
public class RpcTornaBuilder {

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
        config.setParamsDataToTree(true);
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInit(config);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<RpcApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        buildTorna(apiDocList, config, javaProjectBuilder);
    }

    public static void buildTorna(List<RpcApiDoc> apiDocs, ApiConfig apiConfig, JavaProjectBuilder builder) {
        TornaApi tornaApi = new TornaApi();
        tornaApi.setAuthor(StringUtils.isEmpty(apiConfig.getAuthor()) ? System.getProperty("user.name") : apiConfig.getAuthor());
        tornaApi.setIsReplace((apiConfig.getReplace() == null || apiConfig.getReplace()) ? 1 : 0);
        Apis api;
        List<Apis> apisList = new ArrayList<>();
        //添加接口数据
        for (RpcApiDoc a : apiDocs) {
            api = new Apis();
            api.setName(StringUtils.isBlank(a.getDesc()) ? a.getName() : a.getDesc());
            TornaUtil.setDebugEnv(apiConfig, tornaApi);
            api.setItems(TornaUtil.buildDubboApis(a.getList()));
            api.setIsFolder(TornaConstants.YES);
            api.setAuthor(a.getAuthor());
            api.setDubboInfo(new DubboInfo().builder()
                    .setAuthor(a.getAuthor())
                    .setProtocol(a.getProtocol())
                    .setVersion(a.getVersion())
                    .setDependency(TornaUtil.buildDependencies(apiConfig.getRpcApiDependencies()))
                    .setInterfaceName(a.getName()));
            api.setOrderIndex(a.getOrder());
            apisList.add(api);
        }
        tornaApi.setCommonErrorCodes(TornaUtil.buildErrorCode(apiConfig));
        tornaApi.setApis(apisList);
        //Build push document information
        Map<String, String> requestJson = TornaConstants.buildParams(TornaConstants.PUSH, new Gson().toJson(tornaApi), apiConfig);

        //Push dictionary information
        Map<String, Object> dicMap = new HashMap<>(2);
        List<TornaDic> docDicts = TornaUtil.buildTornaDic(DocUtil.buildDictionary(apiConfig, builder));

        if (CollectionUtil.isNotEmpty(docDicts)) {
            dicMap.put("enums", docDicts);
            Map<String, String> dicRequestJson = TornaConstants.buildParams(TornaConstants.ENUM_PUSH, new Gson().toJson(dicMap), apiConfig);
            String dicResponseMsg = OkHttp3Util.syncPostJson(apiConfig.getOpenUrl(), new Gson().toJson(dicRequestJson));
//            String dicResponseMsg = "";
            TornaUtil.printDebugInfo(apiConfig, dicResponseMsg, dicRequestJson, TornaConstants.ENUM_PUSH);
        }
        Map<String, String> dicRequestJson = TornaConstants.buildParams(TornaConstants.ENUM_PUSH, new Gson().toJson(dicMap), apiConfig);
        //Get the response result
        String responseMsg = OkHttp3Util.syncPostJson(apiConfig.getOpenUrl(), new Gson().toJson(requestJson));
//        String responseMsg = "";
        //Print the log of pushing documents to Torna
        TornaUtil.printDebugInfo(apiConfig, responseMsg, requestJson, TornaConstants.PUSH);
    }
}
