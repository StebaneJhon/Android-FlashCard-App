package com.ssoaharison.recall.util

class Calculations {
    fun percentageOfRest(all: Int, part: Int) = (100 - ((part.toFloat() / all.toFloat()) * 100)).toInt()
    fun percentageOfPart(all: Int, part: Int) = ((part.toFloat() / all.toFloat()) * 100).toInt()
    fun fractionOfPart(all: Int, part: Int) = (1.0 - (part.toFloat() / all.toFloat())).toFloat()
}