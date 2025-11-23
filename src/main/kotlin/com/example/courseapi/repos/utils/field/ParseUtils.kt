package com.example.courseapi.repos.utils.field

import com.example.courseapi.models.field.Field
import org.jsoup.Jsoup

fun parseTerms(html: String): List<Field> {
    val doc = Jsoup.parse(html)
    return doc.select("select#termFilter option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
}

fun parseAllFields(html: String): Map<String, List<Field>> {
    val doc = Jsoup.parse(html)
    val result = mutableMapOf<String, List<Field>>()
    result["attributes"] = doc.select("select#sectionFilterAttributes option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
    result["terms"] = doc.select("select#termFilter option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
    result["delivery"] = doc.select("input.deliveryTypeCheckBox[value]").map { input ->
        Field(input.attr("value").trim())
    }.filter { it.name.isNotEmpty() }
    result["campuses"] = doc.select("select#campusFilter option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }.filter { it.name.isNotEmpty() }
    result["subjects"] = doc.select("select#subject option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }.filter { it.name.isNotEmpty() }
    result["waitlist"] = doc.select("select#openWaitlist option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
    result["levels"] = doc.select("select#levelFilter option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
    result["days"] = doc.select("select#daysFilter option[value]").map { opt ->
        Field(opt.attr("value").trim())
    }
    return result
}
