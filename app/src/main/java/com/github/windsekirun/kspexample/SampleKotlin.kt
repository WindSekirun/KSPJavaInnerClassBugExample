package com.github.windsekirun.kspexample

class SampleKotlin {
    @SampleAnnotation
    var a: String = ""

    @SampleAnnotation
    var b = 0

    enum class Type {
        Test
    }

    @SampleAnnotation
    var type: Type = Type.Test
}
