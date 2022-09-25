package nl.basjes.experiments.springgqltest.graphql.utils;

import graphql.schema.GraphQLSchema;

@FunctionalInterface
public interface GraphQLSchemaPatcher {

    /**
     * Patch/change the provided {@link GraphQLSchema} instance.
     * @param schema The schema that must be patched
     */
    GraphQLSchema patchSchema(GraphQLSchema schema);

}
