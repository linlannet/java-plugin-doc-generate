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
package io.linlan.doc.builder;

import com.google.gson.Gson;
import io.linlan.doc.common.util.CollectionUtil;
import io.linlan.doc.common.util.OkHttp3Util;
import io.linlan.doc.utils.StringUtils;
import io.linlan.doc.constants.TornaConstants;
import io.linlan.doc.factory.BuildTemplateFactory;
import io.linlan.doc.model.ApiConfig;
import io.linlan.doc.model.ApiDoc;
import io.linlan.doc.model.torna.Apis;
import io.linlan.doc.model.torna.TornaApi;
import io.linlan.doc.model.torna.TornaDic;
import io.linlan.doc.template.IDocBuildTemplate;
import io.linlan.doc.utils.DocUtil;
import io.linlan.doc.utils.TornaUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author xingzi 2021/2/2 18:05
 **/
public class TornaBuilder {

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
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config,true);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<ApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        apiDocList = docBuildTemplate.handleApiGroup(apiDocList, config);
        buildTorna(apiDocList, config, javaProjectBuilder);
    }

    /**
     * build torna Data
     *
     * @param apiDocs   apiData
     * @param apiConfig ApiConfig
     * @param builder   JavaProjectBuilder
     */
    public static void buildTorna(List<ApiDoc> apiDocs, ApiConfig apiConfig, JavaProjectBuilder builder) {
        TornaApi tornaApi = new TornaApi();
        tornaApi.setAuthor(StringUtils.isEmpty(apiConfig.getAuthor()) ? System.getProperty("user.name") : apiConfig.getAuthor());
        tornaApi.setIsReplace((apiConfig.getReplace() == null || apiConfig.getReplace()) ? 1 : 0);
        Apis api;
        List<Apis> groupApiList = new ArrayList<>();
        //Convert ApiDoc to Apis
        for (ApiDoc groupApi : apiDocs) {
            List<Apis> apisList = new ArrayList<>();
            List<ApiDoc> childrenApiDocs = groupApi.getChildrenApiDocs();
            for (ApiDoc a : childrenApiDocs) {
                api = new Apis();
                api.setName(StringUtils.isBlank(a.getDesc()) ? a.getName() : a.getDesc());
                api.setItems(TornaUtil.buildApis(a.getList(), TornaUtil.setDebugEnv(apiConfig, tornaApi)));
                api.setIsFolder(TornaConstants.YES);
                api.setAuthor(a.getAuthor());
                api.setOrderIndex(a.getOrder());
                apisList.add(api);
            }
            api = new Apis();
            api.setName(StringUtils.isBlank(groupApi.getDesc()) ? groupApi.getName() : groupApi.getDesc());
            api.setAuthor(tornaApi.getAuthor());
            api.setOrderIndex(groupApi.getOrder());
            api.setIsFolder(TornaConstants.YES);
            api.setItems(apisList);
            groupApiList.add(api);

        }
        tornaApi.setCommonErrorCodes(TornaUtil.buildErrorCode(apiConfig));
        // delete default group when only default group
        tornaApi.setApis(groupApiList.size() == 1 ? groupApiList.get(0).getItems() : groupApiList);
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
        //Get the response result
        String responseMsg = OkHttp3Util.syncPostJson(apiConfig.getOpenUrl(), new Gson().toJson(requestJson));
//        String responseMsg = "";
                //Print the log of pushing documents to Torna
        TornaUtil.printDebugInfo(apiConfig, responseMsg, requestJson, TornaConstants.PUSH);

    }
}

