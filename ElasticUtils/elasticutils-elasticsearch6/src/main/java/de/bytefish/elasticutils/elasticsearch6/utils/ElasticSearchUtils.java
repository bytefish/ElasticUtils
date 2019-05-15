// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch6.utils;

import de.bytefish.elasticutils.elasticsearch6.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.exceptions.CreateIndexFailedException;
import de.bytefish.elasticutils.exceptions.IndicesExistsFailedException;
import de.bytefish.elasticutils.exceptions.PutMappingFailedException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ElasticSearchUtils {

    private static final Logger log = Loggers.getLogger(ElasticSearchUtils.class, ElasticSearchUtils.class.getName());

    private ElasticSearchUtils() {

    }

    public static IndicesExistsResponse indexExist(Client client, String indexName) {
        try {
            return client.admin().indices()
                    .prepareExists(indexName)
                    .execute().actionGet();
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Checking Index Exist", e);
            }
            throw new IndicesExistsFailedException(indexName, e);
        }
    }

    public static CreateIndexResponse createIndex(Client client, String indexName) {
        try {
            return internalCreateIndex(client, indexName);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new CreateIndexFailedException(indexName, e);
        }
    }

    public static AcknowledgedResponse putMapping(Client client, String indexName, IElasticSearchMapping mapping) {
        try {
            return internalPutMapping(client, indexName, mapping);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new PutMappingFailedException(indexName, e);
        }
    }

    private static CreateIndexResponse internalCreateIndex(Client client, String indexName) throws IOException {
        final CreateIndexRequestBuilder createIndexRequestBuilder = client
                .admin() // Get the Admin interface...
                .indices() // Get the Indices interface...
                .prepareCreate(indexName); // We want to create a new index ....

        final CreateIndexResponse indexResponse = createIndexRequestBuilder.execute().actionGet();

        if(log.isDebugEnabled()) {
            log.debug("CreatedIndexResponse: isAcknowledged {}", indexResponse.isAcknowledged());
        }

        return indexResponse;
    }

    private static AcknowledgedResponse internalPutMapping(Client client, String indexName, IElasticSearchMapping mapping) throws IOException {

        String json = Strings.toString(mapping.getMapping());

        final PutMappingRequest putMappingRequest = new PutMappingRequest(indexName)
                .type(mapping.getIndexType())
                .source(json, XContentType.JSON);

        final AcknowledgedResponse putMappingResponse = client
                .admin()
                .indices()
                .putMapping(putMappingRequest)
                .actionGet();

        if(log.isDebugEnabled()) {
            log.debug("PutMappingResponse: isAcknowledged {}", putMappingResponse.isAcknowledged());
        }

        return putMappingResponse;
    }

}
