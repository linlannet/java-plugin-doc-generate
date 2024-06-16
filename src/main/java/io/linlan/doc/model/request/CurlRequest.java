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
package io.linlan.doc.model.request;

import io.linlan.doc.model.ApiReqParam;

import java.util.List;

/**
 * @author yu 2020/12/21.
 */
public class CurlRequest {

    private String type;

    private List<ApiReqParam> reqHeaders;

    private String url;

    private String body;

    private String contentType;

    public static CurlRequest builder() {
        return new CurlRequest();
    }

    public String getType() {
        return type;
    }

    public CurlRequest setType(String type) {
        this.type = type;
        return this;
    }

    public List<ApiReqParam> getReqHeaders() {
        return reqHeaders;
    }

    public CurlRequest setReqHeaders(List<ApiReqParam> reqHeaders) {
        this.reqHeaders = reqHeaders;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public CurlRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getBody() {
        return body;
    }

    public CurlRequest setBody(String body) {
        this.body = body;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public CurlRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
