package com.example.courseapi.resolvers.utils.schedule

import com.example.courseapi.models.schedule.*
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun scheduleSafe(action: suspend () -> List<Schedule>): ScheduleResult =
    safeExecute(
        action,
        { SuccessSchedule(it) },
        { code, msg -> ErrorSchedule(code, msg) }
    )