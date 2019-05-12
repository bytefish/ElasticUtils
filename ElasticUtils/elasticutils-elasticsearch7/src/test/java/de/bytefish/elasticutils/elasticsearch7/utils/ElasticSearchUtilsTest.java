// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.utils;

import de.bytefish.elasticutils.elasticsearch7.utils.ElasticSearchUtils;
import de.bytefish.elasticutils.exceptions.CreateIndexFailedException;
import de.bytefish.elasticutils.exceptions.IndicesExistsFailedException;
import de.bytefish.elasticutils.exceptions.PutMappingFailedException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ElasticSearchUtilsTest {

    @Mock
    private RestHighLevelClient client;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void indices_exist_throws_correct_exception_when_underlying_client_throws() throws Exception {

        when(client.indices()).thenThrow(new RuntimeException());

        TestUtils.assertThrows(() -> ElasticSearchUtils.indexExist(client, ""), IndicesExistsFailedException.class);
    }

    @Test
    public void create_index_throws_correct_exception_when_underlying_client_throws() throws Exception {

        when(client.indices()).thenThrow(new RuntimeException());

        TestUtils.assertThrows(() -> ElasticSearchUtils.createIndex(client, ""), CreateIndexFailedException.class);
    }


    @Test
    public void put_mapping_throws_correct_exception_when_underlying_client_throws() throws Exception {

        when(client.indices()).thenThrow(new RuntimeException());

        TestUtils.assertThrows(() -> ElasticSearchUtils.putMapping(client, "", null), PutMappingFailedException.class);
    }
}
