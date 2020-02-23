package com.tt.esayweb3j.impl

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

val gson = GsonBuilder()
    .addDeserializationExclusionStrategy(object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return (f?.annotations?.find { it is Expose } as? Expose)?.deserialize == false
        }

    })
    .addSerializationExclusionStrategy(object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return (f?.annotations?.find { it is Expose } as? Expose)?.serialize == false
        }
    })
    .setPrettyPrinting()
    .create()