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
package io.linlan.plugin.mojo;

import io.linlan.doc.builder.PostmanJsonBuilder;
import io.linlan.doc.model.ApiConfig;
import io.linlan.plugin.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author xingzi 2019/12/07 17:35
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.POSTMAN_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class PostManMojo extends BaseDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            PostmanJsonBuilder.buildPostmanCollection(apiConfig, javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
