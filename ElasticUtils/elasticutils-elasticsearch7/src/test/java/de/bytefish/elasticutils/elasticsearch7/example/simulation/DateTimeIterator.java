// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.example.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

public class DateTimeIterator implements Iterator<LocalDateTime> {

    private LocalDateTime  endDate;
    private LocalDateTime currentDate;
    private Duration interval;

    public DateTimeIterator(LocalDateTime startDate, LocalDateTime endDate, Duration interval) {
        this.endDate = endDate;
        this.interval = interval;
        this.currentDate = startDate;
    }

    @Override
    public boolean hasNext() {
        return currentDate.isBefore(endDate);
    }

    @Override
    public LocalDateTime next() {
        final LocalDateTime result = currentDate;
        currentDate = currentDate.plus(interval);
        return result;
    }
}