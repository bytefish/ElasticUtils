// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch2.mapping;

import com.google.common.collect.ImmutableMap;
import de.bytefish.elasticutils.exceptions.GetMappingFailedException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.object.RootObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseElasticSearchMapping implements IElasticSearchMapping {

    private final String indexType;
    private final Version version;

    public BaseElasticSearchMapping(String indexType, Version version) {
        this.indexType = indexType;
        this.version = version;
    }

    public XContentBuilder getMapping() {
        try {
            return internalGetMapping();
        } catch(Exception e) {
            throw new GetMappingFailedException(indexType, e);
        }
    }

    public String getIndexType() {
        return indexType;
    }

    public Version getVersion() { return version; }

    public XContentBuilder internalGetMapping() throws IOException {

        // Configure the RootObjectMapper:
        RootObjectMapper.Builder rootObjectMapperBuilder = getRootObjectBuilder();

        // Populate the Settings:
        Settings.Builder settingsBuilder = getSettingsBuilder();

        // Build the Mapping:
        Mapping mapping = new Mapping(
                version,
                rootObjectMapperBuilder.build(new Mapper.BuilderContext(settingsBuilder.build(), new ContentPath())),
                getMetaDataFieldMappers(),
                getSourceTransforms(),
                getMetaData());

        // Turn it into JsonXContent:
        return mapping.toXContent(JsonXContent.contentBuilder().startObject(), ToXContent.EMPTY_PARAMS);
    }

    private RootObjectMapper.Builder getRootObjectBuilder() {
        RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder(indexType);

        configureRootObjectBuilder(rootObjectMapperBuilder);

        return rootObjectMapperBuilder;
    }

    private Settings.Builder getSettingsBuilder() {

        Settings.Builder settingsBuilder = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, version)
                .put(IndexMetaData.SETTING_CREATION_DATE, System.currentTimeMillis());

        configureSettingsBuilder(settingsBuilder);

        return settingsBuilder;
    }

    private MetadataFieldMapper[] getMetaDataFieldMappers() {

        List<MetadataFieldMapper> metadataFieldMapper = new ArrayList<>();

        configureMetaDataFieldMappers(metadataFieldMapper);

        return metadataFieldMapper.toArray(new MetadataFieldMapper[metadataFieldMapper.size()]);
    }

    protected Mapping.SourceTransform[] getSourceTransforms() {
        List<Mapping.SourceTransform> sourceTransforms = new ArrayList<>();

        configureSourceTransforms(sourceTransforms);

        return sourceTransforms.toArray(new Mapping.SourceTransform[sourceTransforms.size()]);
    }
    
    private ImmutableMap<String, Object> getMetaData()
    {
        ImmutableMap.Builder<String, Object> metaDataBuilder = new ImmutableMap.Builder<>();

        configureMetaDataBuilder(metaDataBuilder);

        return metaDataBuilder.build();
    }

    protected abstract void configureRootObjectBuilder(RootObjectMapper.Builder builder);

    protected void configureSettingsBuilder(Settings.Builder builder) {}

    protected void configureMetaDataFieldMappers(List<MetadataFieldMapper> metadataFieldMapper) { }

    protected void configureSourceTransforms(List<Mapping.SourceTransform> sourceTransforms) { }

    protected void configureMetaDataBuilder(ImmutableMap.Builder<String, Object> metaDataBuilder) { }

}
