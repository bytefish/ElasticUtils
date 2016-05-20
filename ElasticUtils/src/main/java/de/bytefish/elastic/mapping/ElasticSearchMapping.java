// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elastic.mapping;

import org.elasticsearch.common.xcontent.XContentBuilder;

public interface ElasticSearchMapping {

    XContentBuilder getMapping();

    String getIndexType();

}
