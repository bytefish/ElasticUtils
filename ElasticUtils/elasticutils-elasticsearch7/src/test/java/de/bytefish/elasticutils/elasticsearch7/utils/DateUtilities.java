// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.elasticutils.elasticsearch7.utils;

import java.time.*;
import java.util.Date;

public class DateUtilities {

    public static Date from(LocalDate localDate, LocalTime localTime) {
        return from(localDate, localTime, ZoneOffset.UTC);
    }

    public static Date from(LocalDate localDate, LocalTime localTime, ZoneOffset zoneOffset) {
        LocalDateTime localDateTime = localDate.atTime(localTime);

        return from(localDateTime, zoneOffset);
    }

    public static Date from(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        OffsetDateTime zdt = localDateTime.atOffset(zoneOffset);

        return Date.from(zdt.toInstant());
    }

}