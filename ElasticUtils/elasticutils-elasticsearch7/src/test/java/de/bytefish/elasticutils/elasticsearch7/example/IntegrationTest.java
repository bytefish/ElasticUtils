// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.example;

import de.bytefish.elasticutils.elasticsearch7.client.ElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch7.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.elasticsearch7.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.elasticsearch7.example.simulation.LocalWeatherDataSimulator;
import de.bytefish.elasticutils.elasticsearch7.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.elasticsearch7.mapping.LocalWeatherDataMapper;
import de.bytefish.elasticutils.elasticsearch7.model.LocalWeatherData;
import de.bytefish.elasticutils.elasticsearch7.utils.ElasticSearchUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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

        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")
                )
        );


        // Create the Index, if it doesn't exist yet:
        createIndex(restHighLevelClient, indexName);

        // Create the Mapping, if it doesn't exist yet:
        createMapping(restHighLevelClient, indexName, mapping);

        // Now wrap the Elastic client in our bulk processing client:
        ElasticSearchClient<LocalWeatherData> client = new ElasticSearchClient<>(restHighLevelClient, indexName, mapping, bulkConfiguration);

        // Create some data to work with:
        try (Stream<LocalWeatherData> stream = simulator.generate()) {
            // Consume the Stream with the ElasticSearchClient:
            client.index(stream);
        }

        // The Bulk Insert is asynchronous, we give ElasticSearch some time to do the insert:
        client.awaitClose(1, TimeUnit.SECONDS);
    }

    private void createIndex(RestHighLevelClient client, String indexName) {
        if(!ElasticSearchUtils.indexExist(client, indexName)) {
            ElasticSearchUtils.createIndex(client, indexName);
        }
    }

    private void createMapping(RestHighLevelClient client, String indexName, IElasticSearchMapping mapping) {
        if(ElasticSearchUtils.indexExist(client, indexName)) {
            ElasticSearchUtils.createOrUpdatemapping(client, indexName, mapping);
        }
    }
}