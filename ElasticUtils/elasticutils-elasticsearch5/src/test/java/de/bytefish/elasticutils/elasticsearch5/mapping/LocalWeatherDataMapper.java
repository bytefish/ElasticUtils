// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch5.mapping;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.ScaledFloatFieldMapper;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.RootObjectMapper;

public class LocalWeatherDataMapper extends BaseElasticSearchMapping {

    private static final String INDEX_TYPE = "document";

    public LocalWeatherDataMapper() {
        super(INDEX_TYPE, Version.V_5_0_0);
    }

    @Override
    protected void configureRootObjectBuilder(RootObjectMapper.Builder builder) {
        builder
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
                                .enableGeoHash(false))
                        .nested(ObjectMapper.Nested.newNested(true, false)));
    }
}