// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.example;

import de.bytefish.elasticutils.client.ElasticSearchClient;
import de.bytefish.elasticutils.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.example.simulation.LocalWeatherDataSimulator;
import de.bytefish.elasticutils.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.mapping.LocalWeatherDataMapper;
import de.bytefish.elasticutils.model.LocalWeatherData;
import de.bytefish.elasticutils.utils.ElasticSearchUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.client.Requests.refreshRequest;

@Ignore("Integration Test with Fake Data")
public class IntegrationTest {

    @Test
    public void bulkProcessingTest() throws Exception {

        // Weather Data Simulation between 2013-01-01 and 2013-01-03 in 15 Minute intervals:
        LocalWeatherDataSimulator simulator = new LocalWeatherDataSimulator(
                LocalDateTime.of(2013, 1, 1, 0, 0),
                LocalDateTime.of(2013, 1, 3, 0, 0),
                Duration.ofMinutes(15));

        // Index to work on:
        String indexName = "weather_data";

        // Describes how to build the Index:
        LocalWeatherDataMapper mapping = new LocalWeatherDataMapper();

        // Bulk Options for the Wrapped Client:
        BulkProcessorConfiguration bulkConfiguration = new BulkProcessorConfiguration(BulkProcessingOptions.builder()
                .setBulkActions(100)
                .build());

        // Create a new TransportClient with the default options:
        try (TransportClient transportClient = TransportClient.builder().build()) {

            // Add the Transport Address to the TransportClient:
            transportClient
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

            // Create the Index, if it doesn't exist yet:
            createIndex(transportClient, indexName);

            // Create the Mapping, if it doesn't exist yet:
            createMapping(transportClient, indexName, mapping);

            // Now wrap the Elastic client in our bulk processing client:
            ElasticSearchClient<LocalWeatherData> client = new ElasticSearchClient<>(transportClient, indexName, mapping, bulkConfiguration);

            // Create some data to work with:
            try (Stream<LocalWeatherData> stream = simulator.generate()) {
                // Consume the Stream with the ElasticSearchClient:
                client.index(stream);
            }

            // The Bulk Insert is asynchronous, so we have to wait some time for ElasticSearch to do the insert:
            Thread.sleep(Duration.ofSeconds(5).toMillis());
        }
    }

    private void createIndex(Client client, String indexName) {
        if(!ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.createIndex(client, indexName);
        }
    }

    private void createMapping(Client client, String indexName, IElasticSearchMapping mapping) {
        if(ElasticSearchUtils.indexExist(client, indexName).isExists()) {
            ElasticSearchUtils.putMapping(client, indexName, mapping);
        }
    }
}