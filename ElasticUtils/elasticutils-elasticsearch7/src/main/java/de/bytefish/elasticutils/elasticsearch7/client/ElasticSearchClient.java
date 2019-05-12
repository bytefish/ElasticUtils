// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.client;

import de.bytefish.elasticutils.client.IElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch7.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.elasticsearch7.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.utils.JsonUtilities;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ElasticSearchClient<TEntity> implements IElasticSearchClient<TEntity> {

    private final RestHighLevelClient client;
    private final String indexName;
    private final IElasticSearchMapping mapping;
    private final BulkProcessor bulkProcessor;

    public ElasticSearchClient(final RestHighLevelClient client, final String indexName, final IElasticSearchMapping mapping, final BulkProcessorConfiguration bulkProcessorConfiguration) {
        this.client = client;
        this.indexName = indexName;
        this.mapping = mapping;
        this.bulkProcessor = bulkProcessorConfiguration.build(client);
    }

    public void index(TEntity entity) {
        index(Arrays.asList(entity));
    }

    public void index(List<TEntity> entities) {
        index(entities.stream());
    }

    public void index(Stream<TEntity> entities) {
        entities
                .map(x -> JsonUtilities.convertJsonToBytes(x))
                .filter(x -> x.isPresent())
                .map(x -> createIndexRequest(x.get()))
                .forEach(bulkProcessor::add);
    }

    private IndexRequest createIndexRequest(byte[] messageBytes) {
        IndexRequest request = new IndexRequest();

        request.index(indexName);
        request.source(new BytesArray(messageBytes), XContentType.JSON);

        return request;
    }

    public void flush() {
        bulkProcessor.flush();
    }

    public synchronized boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException {
        return bulkProcessor.awaitClose(timeout, unit);
    }

    @Override
    public void close() throws Exception {
        bulkProcessor.close();
    }
}
