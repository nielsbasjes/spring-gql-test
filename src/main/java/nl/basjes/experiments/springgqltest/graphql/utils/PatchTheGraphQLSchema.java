/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.experiments.springgqltest.graphql.utils;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PatchTheGraphQLSchema {

    private final ApplicationContext context;

    @Autowired
    public PatchTheGraphQLSchema(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Bean
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
        return builder -> builder
            .schemaFactory(
                (typeDefinitionRegistry, runtimeWiring) -> {
                    // First we create the base Schema.
                    GraphQLSchema schema =  new SchemaGenerator()
                        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

                    for (GraphQLSchemaPatcher patcher : context.getBeanProvider(GraphQLSchemaPatcher.class)) {
                        schema = patcher.patchSchema(schema);
                    }
                    return schema;
                }
            );
    }
}
