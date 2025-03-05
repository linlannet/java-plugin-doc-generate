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
package net.linlan.plugin.chain;

import org.apache.maven.artifact.Artifact;

/**
 * @author yu 2020/1/13.
 */
public class CommonArtifactFilterChain implements FilterChain {

    private FilterChain filterChain;

    @Override
    public void setNext(FilterChain nextInChain) {
        this.filterChain = nextInChain;
    }

    @Override
    public boolean ignoreArtifactById(Artifact artifact) {
        String artifactId = artifact.getArtifactId();
        switch (artifactId) {
            case "bcprov-jdk15on":
            case "lombok":
            case "jsqlparser":
            case "disruptor":
            case "snakeyaml":
            case "spring-boot-autoconfigure":
            case "HikariCP":
            case "mysql-connector-java":
            case "classmate":
            case "commons-codec":
            case "commons-lang3":
            case "commons-text":
            case "commons-beanutils":
            case "commons-beanutils-core":
            case "spring-web":
            case "spring-webmvc":
            case "hibernate-validator":
            case "xstream":
            case "guava":
            case "spring-tx":
            case "javassist":
            case "javafaker":
            case "qdox":
            case "gson":
            case "netty-all":
            case "javacv-platform":
                return true;
            default:
                return this.ignore(filterChain, artifact);
        }
    }
}
