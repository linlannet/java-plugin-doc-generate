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
package io.linlan.doc.constants;

import io.linlan.doc.utils.StringUtils;

/**
 * Doc Generate Supported Framework
 *
 * @author yu 2021/6/27.
 */
public enum FrameworkEnum {

    /**
     * Apache Dubbo
     */
    DUBBO("dubbo", "io.linlan.doc.template.RpcDocBuildTemplate"),

    /**
     * Spring Framework
     */
    SPRING("spring", "io.linlan.doc.template.SpringBootDocBuildTemplate"),

    /**
     * JAX-RS
     */
    JAX_RS("JAX-RS", "io.linlan.doc.template.JaxrsDocBuildTemplate");

    /**
     * Framework name
     */
    private String framework;

    /**
     * Framework  IDocBuildTemplate implement
     */
    private String className;


    FrameworkEnum(String framework, String className) {
        this.framework = framework;
        this.className = className;
    }

    public static String getClassNameByFramework(String framework) {
        String className = "";
        if (StringUtils.isEmpty(framework)) {
            return className;
        }
        for (FrameworkEnum e : FrameworkEnum.values()) {
            if (e.framework.equalsIgnoreCase(framework)) {
                className = e.className;
                break;
            }
        }
        return className;
    }


    public String getFramework() {
        return framework;
    }

    public String getClassName() {
        return className;
    }
}
