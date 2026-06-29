package com.example.moodlegovapp.domain.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import kotlin.math.roundToInt

class IntFromNumberAdapter : JsonDeserializer<Int?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Int? {
        if (json == null || json.isJsonNull) return null
        return runCatching { json.asDouble.roundToInt() }.getOrNull()
    }
}


class IntFromBooleanAdapter : JsonDeserializer<Boolean?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Boolean? {
        if (json == null || json.isJsonNull) return null
        return runCatching {
            when {
                json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asInt != 0
                else -> json.asString.equals("true", ignoreCase = true) || json.asString == "1"
            }
        }.getOrNull()
    }
}
