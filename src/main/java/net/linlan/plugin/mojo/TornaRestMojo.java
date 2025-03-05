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
package net.linlan.plugin.mojo;

import net.linlan.doc.builder.TornaBuilder;
import net.linlan.doc.model.ApiConfig;
import net.linlan.plugin.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Send API documents to Torna
 *
 * @author yu 2021/3/11.
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.TORNA_REST_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class TornaRestMojo extends BaseDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            TornaBuilder.buildApiDoc(apiConfig, javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
