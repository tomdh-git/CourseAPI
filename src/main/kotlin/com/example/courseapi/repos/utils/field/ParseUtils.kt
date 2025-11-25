package com.example.courseapi.repos.utils.field

import com.example.courseapi.models.field.Field
import com.example.courseapi.models.field.ValidFields
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

fun getValidFields(allFields: Map<String,List<Field>>): ValidFields{
    val subjects = allFields["subjects"]?.map { it.name }?.toSet() ?: emptySet()
    val campuses = allFields["campuses"]?.map { it.name }?.toSet() ?: emptySet()
    val terms = allFields["terms"]?.map { it.name }?.toSet() ?: emptySet()
    val deliveryTypes = allFields["delivery"]?.map { it.name }?.toSet() ?: emptySet()
    val levels = allFields["levels"]?.map { it.name }?.toSet() ?: emptySet()
    val days = allFields["days"]?.map { it.name }?.toSet() ?: emptySet()
    val waitlistTypes = allFields["waitlist"]?.map { it.name }?.toSet() ?: emptySet()
    val attributes = allFields["attributes"]?.map { it.name }?.toSet() ?: emptySet()
    val fields = ValidFields(
        subjects = subjects,
        campuses = campuses,
        terms = terms,
        deliveryTypes = deliveryTypes,
        levels = levels,
        days = days,
        waitlistTypes = waitlistTypes,
        attributes = attributes
    )
    return fields
}