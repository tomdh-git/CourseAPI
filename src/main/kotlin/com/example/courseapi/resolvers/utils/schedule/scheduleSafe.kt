package com.example.courseapi.resolvers.utils.schedule

import com.example.courseapi.models.schedule.ErrorSchedule
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.models.schedule.ScheduleResult
import com.example.courseapi.models.schedule.SuccessSchedule
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun scheduleSafe(action: suspend () -> List<Schedule>): ScheduleResult =
    safeExecute(
        action,
        { SuccessSchedule(it) },
        { code, msg -> ErrorSchedule(code, msg) }
    )