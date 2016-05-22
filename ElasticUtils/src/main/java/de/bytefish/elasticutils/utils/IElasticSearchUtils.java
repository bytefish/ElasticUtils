// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.utils;

import de.bytefish.elasticutils.mapping.IElasticSearchMapping;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;

public interface IElasticSearchUtils {

    IndicesExistsResponse indexExist(String indexName);

    CreateIndexResponse createIndex(String indexName);

    PutMappingResponse putMapping(String indexName, IElasticSearchMapping mapping);

}
