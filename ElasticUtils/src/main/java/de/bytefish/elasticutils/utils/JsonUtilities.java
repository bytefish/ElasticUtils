// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.util.Optional;

public class JsonUtilities {

    private static final ESLogger log = ESLoggerFactory.getLogger(JsonUtilities.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <TEntity> Optional<byte[]> convertJsonToBytes(TEntity entity) {
        try {
            return Optional.empty().of(mapper.writeValueAsBytes(entity));
        } catch(Exception e) {
            if(log.isErrorEnabled()) {
                log.error(String.format("Failed to convert entity %s to JSON", entity), e);
            }
        }
        return Optional.empty();
    }
}
