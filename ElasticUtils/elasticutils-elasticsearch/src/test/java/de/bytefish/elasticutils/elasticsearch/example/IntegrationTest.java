// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch.example;

import de.bytefish.elasticutils.elasticsearch.client.ElasticSearchClient;
import de.bytefish.elasticutils.elasticsearch.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.elasticsearch.client.bulk.options.BulkProcessingOptions;
import de.bytefish.elasticutils.elasticsearch.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.elasticsearch.mapping.LocalWeatherDataMapper;
import de.bytefish.elasticutils.elasticsearch.model.LocalWeatherData;
import de.bytefish.elasticutils.elasticsearch.example.simulation.LocalWeatherDataSimulator;
import de.bytefish.elasticutils.elasticsearch.utils.ElasticSearchUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.json.JsonXContentGenerator;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@Ignore("Integration Test with Fake Data")
public class IntegrationTest {

    @Test
    public void JsonXContentTest() throws Exception {

        Settings.Builder settingsBuilder = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, 1)
                .put(IndexMetaData.SETTING_CREATION_DATE, System.currentTimeMillis());;

        RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder("document")
                .add(new DateFieldMapper.Builder("dateTime"))
                .add(new ScaledFloatFieldMapper.Builder("temperature").scalingFactor(1))
                .add(new ScaledFloatFieldMapper.Builder("windSpeed").scalingFactor(1))
                .add(new ScaledFloatFieldMapper.Builder("stationPressure").scalingFactor(1))
                .add(new StringFieldMapper.Builder("skyCondition"))
                .add(new ObjectMapper.Builder("station")
                        .add(new StringFieldMapper.Builder("wban"))
                        .add(new StringFieldMapper.Builder("name"))
                        .add(new StringFieldMapper.Builder("state"))
                        .add(new StringFieldMapper.Builder("location"))
                        .add(new GeoPointFieldMapper.Builder("coordinates")
                                .enableLatLon(true)
                                .enableGeoHash(false))
                        .nested(ObjectMapper.Nested.newNested(true, false)));


        RootObjectMapper rootObjectMapper = rootObjectMapperBuilder.build(new Mapper.BuilderContext(settingsBuilder.build(), new ContentPath(1)));

           String a = rootObjectMapper.toXContent(XContentFactory.jsonBuilder().startObject(), new ToXContent.MapParams(emptyMap())).endObject().string();
    }

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
        try (TransportClient transportClient = new PreBuiltTransportClient(Settings.EMPTY)) {

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

            // The Bulk Insert is asynchronous, we give ElasticSearch some time to do the insert:
            client.awaitClose(1, TimeUnit.SECONDS);
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