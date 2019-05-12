// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.client.bulk.configuration;

import de.bytefish.elasticutils.elasticsearch7.client.bulk.listener.LoggingBulkProcessorListener;
import de.bytefish.elasticutils.elasticsearch7.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.elasticsearch7.client.bulk.options.BulkProcessingOptionsBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class BulkProcessorConfiguration {

    private BulkProcessingOptions options = new BulkProcessingOptionsBuilder().build();
    private BulkProcessor.Listener listener = new LoggingBulkProcessorListener();

    public BulkProcessorConfiguration(BulkProcessingOptions options)
    {
        this(options, new LoggingBulkProcessorListener());
    }

    public BulkProcessorConfiguration(BulkProcessingOptions options, BulkProcessor.Listener listener) {
        this.options = options;
        this.listener = listener;
    }

    public BulkProcessingOptions getBulkProcessingOptions() {
        return options;
    }

    public BulkProcessor.Listener getBulkProcessorListener() {
        return listener;
    }

    public BulkProcessor build(final RestHighLevelClient client) {

        // Taken from:
        //
        //      * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-bulk.html#java-rest-high-document-bulk-processor
        //
        return BulkProcessor.builder((request, bulkListener) -> {
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        }, listener)
                .setConcurrentRequests(options.getConcurrentRequests())
                .setBulkActions(options.getBulkActions())
                .setBulkSize(options.getBulkSize())
                .setFlushInterval(options.getFlushInterval())
                .setBackoffPolicy(options.getBackoffPolicy())
                .build();
    }
}