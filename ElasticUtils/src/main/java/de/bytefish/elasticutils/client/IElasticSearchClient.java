// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.client;

import de.bytefish.elasticutils.client.bulk.configuration.BulkProcessorConfiguration;
import de.bytefish.elasticutils.mapping.IElasticSearchMapping;
import de.bytefish.elasticutils.utils.JsonUtilities;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public interface IElasticSearchClient<TEntity> extends AutoCloseable {

    void index(TEntity entity);

    void index(List<TEntity> entities);

    void index(Stream<TEntity> entities);

    void flush();

    boolean awaitClose(long timeout, TimeUnit unit) throws InterruptedException;
}
