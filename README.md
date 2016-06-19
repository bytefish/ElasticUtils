# ElasticUtils #

## Description ##

``elasticutils`` is a library for working with Elasticsearch in Java. 

It greatly simplifies working with the Elasticsearch Mapping API and the Bulk Insert API.

## Maven Dependencies ##

You can add the following dependencies to your pom.xml to include ``elasticutils`` in your project.

```xml
<dependency>
	<groupId>de.bytefish</groupId>
	<artifactId>elasticutils</artifactId>
	<version>0.3</version>
</dependency>
```

## Quickstart ##

The Quickstart shows you how to work with ``elasticutils``.

### IntegrationTest ###

```java
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
```

### Model ###

#### LocalWeatherData ####
```java
// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class LocalWeatherData {

    @JsonProperty("station")
    public Station station;

    @JsonProperty("dateTime")
    public Date dateTime;

    @JsonProperty("temperature")
    public Float temperature;

    @JsonProperty("windSpeed")
    public Float windSpeed;

    @JsonProperty("stationPressure")
    public Float stationPressure;

    @JsonProperty("skyCondition")
    public String skyCondition;
}
```

#### Station ####

```java
// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.elasticsearch.common.geo.GeoPoint;

public class Station {

    @JsonProperty("wban")
    public String wban;

    @JsonProperty("name")
    public String name;

    @JsonProperty("state")
    public String state;

    @JsonProperty("location")
    public String location;

    @JsonProperty("coordinates")
    public GeoLocation geoLocation;

}
```

#### GeoLocation ####

```java
// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoLocation {

    @JsonProperty("lat")
    public double lat;

    @JsonProperty("lon")
    public double lon;

    public GeoLocation() {}

    public GeoLocation(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
```

### Mapping ###

```java
// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.mapping;

import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.FloatFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;

public class LocalWeatherDataMapper extends BaseElasticSearchMapping {

    private static final String INDEX_TYPE = "document";

    public LocalWeatherDataMapper() {
        super(INDEX_TYPE, "1.0.0");
    }

    @Override
    protected void configure(RootObjectMapper.Builder builder) {
        builder
                .add(new DateFieldMapper.Builder("dateTime"))
                .add(new FloatFieldMapper.Builder("temperature"))
                .add(new FloatFieldMapper.Builder("windSpeed"))
                .add(new FloatFieldMapper.Builder("stationPressure"))
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
    }

    @Override
    protected void configure(Settings.Builder builder) {
        builder
                .put(IndexMetaData.SETTING_VERSION_CREATED, 1)
                .put(IndexMetaData.SETTING_CREATION_DATE, System.currentTimeMillis());
    }
}
```
