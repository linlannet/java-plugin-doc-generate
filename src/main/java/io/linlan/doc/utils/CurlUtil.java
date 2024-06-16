/*
 * doc-generate
 *
 * Copyright (C) 2018-2021 doc-generate
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.linlan.doc.utils;

import io.linlan.doc.common.util.CollectionUtil;
import io.linlan.doc.constants.DocGlobalConstants;
import io.linlan.doc.model.ApiReqParam;
import io.linlan.doc.model.request.CurlRequest;

/**
 * @author yu 2020/12/21.
 */
public class CurlUtil {

    public static String toCurl(CurlRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("curl");
        sb.append(" -X");
        sb.append(" ").append(request.getType());
        if (request.getUrl().indexOf("https") == 0) {
            sb.append(" -k");
        }
        if (StringUtils.isNotEmpty(request.getContentType()) &&
                !DocGlobalConstants.URL_CONTENT_TYPE.equals(request.getContentType())) {
            sb.append(" -H");
            sb.append(" 'Content-Type: ").append(request.getContentType()).append("'");
        }
        if (CollectionUtil.isNotEmpty(request.getReqHeaders())) {
            for (ApiReqParam reqHeader : request.getReqHeaders()) {
                sb.append(" -H");
                if (StringUtils.isEmpty(reqHeader.getValue())) {
                    sb.append(" '" + reqHeader.getName() + "'");
                } else {
                    sb.append(" '" + reqHeader.getName() + ':' + reqHeader.getValue() + "'");
                }
            }
        }
        sb.append(" -i");
        // append request url
        sb.append(" ").append(request.getUrl());
        if (StringUtils.isNotEmpty(request.getBody())) {
            sb.append(" --data");
            sb.append(" '" + request.getBody() + "'");
        }
        return sb.toString();
    }
}
