// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.mapping;

import de.bytefish.elasticutils.elasticsearch7.mapping.BaseElasticSearchMapping;
import org.elasticsearch.Version;
import org.elasticsearch.index.mapper.*;

public class LocalWeatherDataMapper extends BaseElasticSearchMapping {

    private static final String INDEX_TYPE = "document";

    public LocalWeatherDataMapper() {
        super(INDEX_TYPE, Version.V_7_0_1);
    }

    @Override
    protected void configureRootObjectBuilder(RootObjectMapper.Builder builder) {
        builder
                .add(new DateFieldMapper.Builder("dateTime"))
                .add(new NumberFieldMapper.Builder("temperature", NumberFieldMapper.NumberType.FLOAT))
                .add(new NumberFieldMapper.Builder("windSpeed", NumberFieldMapper.NumberType.FLOAT))
                .add(new NumberFieldMapper.Builder("stationPressure", NumberFieldMapper.NumberType.FLOAT))
                .add(new TextFieldMapper.Builder("skyCondition"))
                .add(new ObjectMapper.Builder("station")
                        .add(new TextFieldMapper.Builder("wban"))
                        .add(new TextFieldMapper.Builder("name"))
                        .add(new TextFieldMapper.Builder("state"))
                        .add(new TextFieldMapper.Builder("location"))
                        .add(new GeoPointFieldMapper.Builder("coordinates"))
                        .nested(ObjectMapper.Nested.newNested(true, false)));
    }
}