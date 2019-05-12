// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.mapping;

import com.google.common.collect.ImmutableMap;
import de.bytefish.elasticutils.exceptions.GetMappingFailedException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyMap;

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

        //new Mapping(arg0, arg1, arg2, arg3)getSourceTransforms(),
        // Build the Mapping:
        Mapping mapping = new Mapping(
                version,
                rootObjectMapperBuilder.build(new Mapper.BuilderContext(settingsBuilder.build(), new ContentPath(1))),
                getMetaDataFieldMappers(),
                getMeta());

        // Turn it into JsonXContent:
        return mapping.toXContent(XContentFactory.jsonBuilder().startObject(), new ToXContent.MapParams(emptyMap())).endObject();
    }

    private Settings.Builder getSettingsBuilder() {

        Settings.Builder settingsBuilder = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, version)
                .put(IndexMetaData.SETTING_CREATION_DATE, System.currentTimeMillis());

        configureSettingsBuilder(settingsBuilder);

        return settingsBuilder;
    }

    private RootObjectMapper.Builder getRootObjectBuilder() {

        RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder(indexType);

        configureRootObjectBuilder(rootObjectMapperBuilder);

        return rootObjectMapperBuilder;
    }

    private MetadataFieldMapper[] getMetaDataFieldMappers() {

        List<MetadataFieldMapper> metadataFieldMapper = new ArrayList<>();

        configureMetaDataFieldMappers(metadataFieldMapper);

        return metadataFieldMapper.toArray(new MetadataFieldMapper[metadataFieldMapper.size()]);
    }

    private ImmutableMap<String, Object> getMeta() {

        ImmutableMap.Builder<String, Object> metaFieldsBuilder = new ImmutableMap.Builder<String, Object>();

        configureMetaFields(metaFieldsBuilder);

        return metaFieldsBuilder.build();
    }

    protected abstract void configureRootObjectBuilder(RootObjectMapper.Builder builder);

    protected void configureSettingsBuilder(Settings.Builder builder) {}

    protected void configureMetaDataFieldMappers(List<MetadataFieldMapper> metadataFieldMapper) { }

    protected void configureMetaFields(ImmutableMap.Builder<String, Object> metaFieldsBuilder) { }
}