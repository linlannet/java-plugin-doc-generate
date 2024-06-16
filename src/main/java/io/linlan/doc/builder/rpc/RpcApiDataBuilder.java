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

import io.linlan.doc.utils.StringUtils;
import io.linlan.doc.model.ApiConfig;
import io.linlan.doc.model.rpc.RpcApiAllData;
import com.thoughtworks.qdox.JavaProjectBuilder;
import io.linlan.doc.constants.FrameworkEnum;

/**
 * @author yu 2020/5/24.
 */
public class RpcApiDataBuilder {

    /**
     * Get list of ApiDoc
     *
     * @param config ApiConfig
     * @return List of ApiDoc
     */
    public static RpcApiAllData getApiData(ApiConfig config) {
        config.setShowJavaType(true);
        if (StringUtils.isEmpty(config.getFramework())) {
            config.setFramework(FrameworkEnum.DUBBO.getFramework());
        }
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInitForGetApiData(config);
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        builderTemplate.getApiData(config, javaProjectBuilder);
        return builderTemplate.getApiData(config, javaProjectBuilder);
    }
}
