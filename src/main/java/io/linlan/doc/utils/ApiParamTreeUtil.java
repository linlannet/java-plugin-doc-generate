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
package io.linlan.doc.utils;

import io.linlan.doc.common.util.CollectionUtil;
import io.linlan.doc.model.ApiParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yu 2020/8/8.
 */
public class ApiParamTreeUtil {

    public static List<ApiParam> apiParamToTree(List<ApiParam> apiParamList) {
        if (CollectionUtil.isEmpty(apiParamList)) {
            return new ArrayList<>(0);
        }
        List<ApiParam> params = new ArrayList<>();
        // find root
        for (ApiParam apiParam : apiParamList) {
            // remove pre of field
            apiParam.setField(apiParam.getField().replaceAll("└─", "").replaceAll("&nbsp;", ""));
            // pid == 0
            if (apiParam.getPid() == 0) {
                params.add(apiParam);
            }
        }
        for (ApiParam apiParam : params) {
            // remove pre of field
            apiParam.setChildren(getChild(apiParam.getId(), apiParamList));
        }
        return params;
    }

    /**
     * find child
     *
     * @param id           param id
     * @param apiParamList List of ApiParam
     * @return List of ApiParam
     */
    private static List<ApiParam> getChild(int id, List<ApiParam> apiParamList) {
        List<ApiParam> childList = new ArrayList<>();
        for (ApiParam param : apiParamList) {
            if (param.getPid() == id) {
                childList.add(param);
            }
        }
        for (ApiParam param : childList) {
            param.setChildren(getChild(param.getId(), apiParamList));
        }
        if (childList.size() == 0) {
            return new ArrayList<>(0);
        }
        return childList;
    }

}
