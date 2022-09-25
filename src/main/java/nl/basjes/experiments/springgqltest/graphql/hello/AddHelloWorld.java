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

package nl.basjes.experiments.springgqltest.graphql.hello;


import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.SchemaTransformer;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.basjes.experiments.springgqltest.graphql.utils.GraphQLSchemaPatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.util.TraversalControl.CONTINUE;

@Configuration(proxyBeanMethods = false)
@SuppressWarnings("java:S1854") // https://community.sonarsource.com/t/unresolved-variable-in-anonymous-class-s1854-remove-this-useless-assignment-to-local-variable-unknown/46076
public class AddHelloWorld {

    @Bean
    GraphQLSchemaPatcher addHelloWorldToSchema() {
        return schema -> {
            // New "function" to be put in Query
            GraphQLFieldDefinition sayHello = newFieldDefinition()
                .name("sayHello")
                .description("Returns 'Just say hello'")
                .type(Scalars.GraphQLString)
                .build();

            DataFetcher<?> sayHelloDataFetcher = environment -> "Just say hello";

            GraphQLTypeVisitorStub visitor = new GraphQLTypeVisitorStub() {
                @Override
                public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType, TraverserContext<GraphQLSchemaElement> context) {
                    GraphQLCodeRegistry.Builder codeRegistry = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

                    if (objectType.getName().equals("Query")) {
                        // Adding an extra field with a type and a data fetcher
                        GraphQLObjectType updatedQuery = objectType.transform(builder -> builder.field(sayHello));
                        FieldCoordinates coordinates = FieldCoordinates.coordinates(objectType.getName(), sayHello.getName());
                        codeRegistry.dataFetcher(coordinates, sayHelloDataFetcher);
                        return changeNode(context, updatedQuery);
                    }
                    return CONTINUE;
                }
            };

            return SchemaTransformer.transformSchema(schema, visitor);
        };
    }


}


