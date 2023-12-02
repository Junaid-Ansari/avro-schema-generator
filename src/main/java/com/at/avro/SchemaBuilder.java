package com.at.avro;

import com.at.avro.config.AvroConfig;
import com.at.avro.config.FormatterConfig;
import com.at.avro.mappers.RemovePlural;
import com.at.avro.mappers.ToCamelCase;

import java.util.Date;
import java.util.List;

public class SchemaBuilder {
    public static void main(String[] args) {
        System.out.println("Starting Schema search!"); // Display the string.
        generateSchema();
    }

    private static void generateSchema(){
        DbSchemaExtractor schemaExtractor = new DbSchemaExtractor("jdbc:sqlserver://localhost:3306", "root", "pass");

        // Some of available configuration options
        AvroConfig avroConfig = new AvroConfig("some.namespace")
            .setRepresentEnumsAsStrings(true) // use 'string' avro type instead of 'enum' for enums
            .setAllFieldsDefaultNull(true)    // adds default: 'null' to fields definition
            .setNullableTrueByDefault(true)   // makes all fields nullable
            .setUseSqlCommentsAsDoc(true)     // use sql comments to fill 'doc' field
            .setSchemaNameMapper(new ToCamelCase().andThen(new RemovePlural())) // specify table name transformation to be used for schema name
            .setUnknownTypeResolver(type -> "string") // specify what to do with custom and unsupported db types
            .setDateTypeClass(Date.class) // add hint for avro compiler about which class to use for dates
            .setAvroSchemaPostProcessor((schema, table) -> {
                // adding some custom properties to avro schema
                schema.addCustomProperty("db-schema-name", "mydb");
                schema.addCustomProperty("db-table-name", table.getName());
            });

// Get avro models for a few tables
        List<AvroSchema> schemas = schemaExtractor.getAll(avroConfig);

// You can specify some formatting options by creating a FormatterConfig and passing it to SchemaGenerator.
        FormatterConfig formatterConfig = FormatterConfig.builder()
            .setPrettyPrintSchema(true)
            .setPrettyPrintFields(false)
            .setIndent("    ")
            .build();

        for (AvroSchema schema : schemas) {
            String schemaJson = SchemaGenerator.generate(schema, formatterConfig);
            System.out.println(schemaJson);
        }
    }
}
