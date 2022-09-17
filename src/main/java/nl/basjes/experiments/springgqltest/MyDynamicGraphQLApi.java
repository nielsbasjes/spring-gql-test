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

package nl.basjes.experiments.springgqltest;


import graphql.Scalars;
import graphql.language.FieldDefinition;
import graphql.language.Node;
import graphql.language.NodeChildrenContainer;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeRuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.language.ObjectTypeDefinition.CHILD_FIELD_DEFINITIONS;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Configuration(proxyBeanMethods = false)
public class MyDynamicGraphQLApi {

    private static final Logger LOG = LogManager.getLogger(MyDynamicGraphQLApi.class);

    @Bean
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
        return builder -> {

            // New type
            GraphQLObjectType version = GraphQLObjectType
                .newObject()
                .name("Version")
                .description("The version info")
                .field(newFieldDefinition()
                    .name("commit")
                    .description("The commit hash")
                    .type(Scalars.GraphQLString)
                    .build())
                .field(newFieldDefinition()
                    .name("url")
                    .description("Where can we find it")
                    .type(Scalars.GraphQLString)
                    .build())
                .build();

            // New "function" to be put in Query
            GraphQLFieldDefinition getVersion = newFieldDefinition()
                .name("getVersion")
                .description("It should return the do something here")
                .argument(GraphQLArgument.newArgument().name("thing").type(Scalars.GraphQLString).build())
                .type(version)
                .build();


            // Wiring for the new type
            TypeRuntimeWiring typeRuntimeWiringVersion =
                TypeRuntimeWiring
                    .newTypeWiring("Version")
                    .dataFetcher("commit", testDataFetcher)
                    .dataFetcher("url", testDataFetcher)
                    .build();

            // Wiring for the new "function"
            TypeRuntimeWiring typeRuntimeWiringQuery =
                    TypeRuntimeWiring
                        .newTypeWiring("Query")
                        .dataFetcher("getVersion", getVersionDataFetcher)
                        .build();

            builder
                .schemaFactory(
                    (typeDefinitionRegistry, runtimeWiring) -> {
                        // NOTE: Spring-Graphql DEMANDS a schema.graphqls with a valid schema or it will not load...

                        // ---------------------------------
                        // Extending the entries in Query

                        // We get the existing Query from the typeDefinitionRegistry (defined in the schema.graphqls file).
                        ObjectTypeDefinition query = (ObjectTypeDefinition) typeDefinitionRegistry.getType("Query").orElseThrow();
                        NodeChildrenContainer namedChildren = query.getNamedChildren();
                        List<Node> fieldDefinitions = namedChildren.getChildren(CHILD_FIELD_DEFINITIONS);

                        // We add all our new "functions" (field Definitions) that need to be added to the Query
                        fieldDefinitions.add(convert(getVersion));

                        // Add them all as extra fields to the existing Query
                        ObjectTypeDefinition queryWithNewChildren = query.withNewChildren(namedChildren);

                        // We remove the old "Query" and replace it with the version that has more children.
                        typeDefinitionRegistry.remove(query);
                        typeDefinitionRegistry.add(queryWithNewChildren);

                        // -----------------------
                        // Add all additional types (outside of Query)
                        typeDefinitionRegistry.add(convert(version));

                        // -----------------------
                        // Add all additional wiring
                        // NASTY 3: There is no simple 'addType' on an existing instance of RuntimeWiring.
                        addType(runtimeWiring, typeRuntimeWiringQuery);
                        addType(runtimeWiring, typeRuntimeWiringVersion);

                        // Now we create the Schema.
                        return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
                    }
                );
        };
    }


    static DataFetcher<?> getVersionDataFetcher = environment -> {
        String arguments = environment
            .getArguments()
            .entrySet()
            .stream()
            .map(entry -> "{ " + entry.getKey() + " = " + entry.getValue().toString() + " }")
            .collect(Collectors.joining(" | "));

        String result = "getVersion Fetch: %s(%s)".formatted(environment.getField(), arguments);

        LOG.info("{}", result);
        return result;
    };

    static DataFetcher<?> testDataFetcher = environment -> {
        String arguments = environment
            .getArguments()
            .entrySet()
            .stream()
            .map(entry -> "{ " + entry.getKey() + " = " + entry.getValue().toString() + " }")
            .collect(Collectors.joining(" | "));

        String result = "Fetch: %s(%s)".formatted(environment.getField().getName(), arguments);

        LOG.info("{}", result);
        return result;
    };


    private FieldDefinition convert(GraphQLFieldDefinition field) {
        // NASTY: So far the only way I have been able to find to this conversion is to
        //   - wrap it in a GraphQLObjectType
        //   - Print it
        //   - and parse that String
        GraphQLObjectType query = GraphQLObjectType
                .newObject()
                .name("DUMMY")
                .field(field)
                .build();

        String print = new SchemaPrinter().print(query);
        ObjectTypeDefinition dummy = (ObjectTypeDefinition)new SchemaParser().parse(print).getType("DUMMY").orElseThrow();
        return dummy.getFieldDefinitions().get(0);
    }

    private TypeDefinition convert(GraphQLObjectType objectType) {
        String print = new SchemaPrinter().print(objectType);
        return new SchemaParser().parse(print).getType(objectType.getName()).orElseThrow();
    }

    // Yes, I know NASTY HACK
    public void addType(RuntimeWiring runtimeWiring, TypeRuntimeWiring typeRuntimeWiring) {
        Map<String, Map<String, DataFetcher>> dataFetchers = runtimeWiring.getDataFetchers();

        String typeName = typeRuntimeWiring.getTypeName();
        Map<String, DataFetcher> typeDataFetchers = dataFetchers.computeIfAbsent(typeName, k -> new LinkedHashMap<>());
        typeDataFetchers.putAll(typeRuntimeWiring.getFieldDataFetchers());

        TypeResolver typeResolver = typeRuntimeWiring.getTypeResolver();
        if (typeResolver != null) {
            runtimeWiring.getTypeResolvers().put(typeName, typeResolver);
        }
    }

}


