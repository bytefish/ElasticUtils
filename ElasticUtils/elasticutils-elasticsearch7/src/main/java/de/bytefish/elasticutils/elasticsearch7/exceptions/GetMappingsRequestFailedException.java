package de.bytefish.elasticutils.elasticsearch7.exceptions;

public class GetMappingsRequestFailedException extends RuntimeException {
    public GetMappingsRequestFailedException(String indexName, Throwable cause) {
        super(String.format("Indices '%s' failed", indexName), cause);
    }
}