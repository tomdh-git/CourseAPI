package com.example.courseapi.resolvers.utils.field

import com.example.courseapi.models.field.*
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun fieldSafe(action: suspend () -> List<Field>): FieldResult =
    safeExecute(
        action,
        { SuccessField(it) },
        { code, msg -> ErrorField(code, msg) }
    )