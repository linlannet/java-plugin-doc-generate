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
package io.linlan.doc.model.postman.request;

import io.linlan.doc.model.postman.UrlBean;
import io.linlan.doc.model.postman.request.body.BodyBean;
import io.linlan.doc.model.postman.request.header.HeaderBean;

import java.util.List;

/**
 * @author xingzi
 */
public class RequestBean {
    private String method;
    private BodyBean body;
    private UrlBean url;
    private String description;
    private List<HeaderBean> header;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BodyBean getBody() {
        return body;
    }

    public void setBody(BodyBean body) {
        this.body = body;
    }

    public UrlBean getUrl() {
        return url;
    }

    public void setUrl(UrlBean url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HeaderBean> getHeader() {
        return header;
    }

    public void setHeader(List<HeaderBean> header) {
        this.header = header;
    }
}
