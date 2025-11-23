package com.example.courseapi.resolvers.utils.field

import com.example.courseapi.models.misc.ErrorField
import com.example.courseapi.models.misc.Field
import com.example.courseapi.models.misc.FieldResult
import com.example.courseapi.models.misc.SuccessField
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun fieldSafe(action: suspend () -> List<Field>): FieldResult =
    safeExecute(
        action,
        { SuccessField(it) },
        { code, msg -> ErrorField(code, msg) }
    )