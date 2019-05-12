// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.utils;

import de.bytefish.elasticutils.elasticsearch7.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.exceptions.CreateIndexFailedException;
import de.bytefish.elasticutils.exceptions.IndicesExistsFailedException;
import de.bytefish.elasticutils.exceptions.PutMappingFailedException;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class ElasticSearchUtils {

    private static final Logger log = Loggers.getLogger(ElasticSearchUtils.class, ElasticSearchUtils.class.getName());

    private ElasticSearchUtils() {

    }

    public static boolean indexExist(RestHighLevelClient client, String indexName) {
        try {

            GetIndexRequest request = new GetIndexRequest("indexName");

            return client.indices().exists(request, RequestOptions.DEFAULT);

        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Checking Index Exist", e);
            }
            throw new IndicesExistsFailedException(indexName, e);
        }
    }

    public static CreateIndexResponse createIndex(RestHighLevelClient client, String indexName) {
        try {
            return internalCreateIndex(client, indexName);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new CreateIndexFailedException(indexName, e);
        }
    }

    public static AcknowledgedResponse putMapping(RestHighLevelClient client, String indexName, IElasticSearchMapping mapping) {
        try {
            return internalPutMapping(client, indexName, mapping);
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error("Error Creating Index", e);
            }
            throw new PutMappingFailedException(indexName, e);
        }
    }

    private static CreateIndexResponse internalCreateIndex(RestHighLevelClient client, String indexName) throws IOException {

        final CreateIndexRequest request = new CreateIndexRequest("indexName");

        final CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);

        if(log.isDebugEnabled()) {
            log.debug("CreatedIndexResponse: isAcknowledged {}", response.isAcknowledged());
        }

        return response;
    }

    private static AcknowledgedResponse internalPutMapping(RestHighLevelClient client, String indexName, IElasticSearchMapping mapping) throws IOException {

        String json = Strings.toString(mapping.getMapping());

        final PutMappingRequest putMappingRequest = new PutMappingRequest(indexName)
                .source(json, XContentType.JSON);

        final AcknowledgedResponse putMappingResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);

        if(log.isDebugEnabled()) {
            log.debug("PutMappingResponse: isAcknowledged {}", putMappingResponse.isAcknowledged());
        }

        return putMappingResponse;
    }

}
