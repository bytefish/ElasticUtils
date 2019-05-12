// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.example.simulation;

import de.bytefish.elasticutils.elasticsearch7.model.GeoLocation;
import de.bytefish.elasticutils.elasticsearch7.model.LocalWeatherData;
import de.bytefish.elasticutils.elasticsearch7.model.Station;
import de.bytefish.elasticutils.elasticsearch7.utils.DateUtilities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LocalWeatherDataSimulator {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Duration interval;

    public LocalWeatherDataSimulator(LocalDateTime startDate, LocalDateTime endDate, Duration interval) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.interval = interval;
    }

    public Stream<LocalWeatherData> generate() {

        // Build the Station:
        final Station station = getStation(getGeoLocation());

        // For Creating the Measurement TimeSteps:
        final DateTimeIterator iterator = new DateTimeIterator(startDate, endDate, interval);

        // Create the Stream:
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .map(measurementTimeStamp -> createLocalWeatherData(station, measurementTimeStamp));

    }

    private GeoLocation getGeoLocation() {
        final GeoLocation geoLocation = new GeoLocation();

        geoLocation.lat = 41.8705f;
        geoLocation.lon = -103.593f;

        return geoLocation;
    }

    private Station getStation(GeoLocation geoLocation) {
        final Station station = new Station();

        station.wban = "WB1931";
        station.state = "NE";
        station.name = "SCOTTSBLUFF";
        station.location = "WESTERN NE REGIONAL/HEILIG FIELD AP";
        station.geoLocation = geoLocation;

        return station;
    }

    private LocalWeatherData createLocalWeatherData(final Station station, final LocalDateTime measuredTime) {
        final LocalWeatherData data = new LocalWeatherData();

        data.dateTime = DateUtilities.from(measuredTime, ZoneOffset.UTC);
        data.station = station;
        data.skyCondition = "CLR";
        data.temperature = 22.0f;
        data.stationPressure = 42.12f;
        data.windSpeed = 5.0f;

        return data;
    }
}