package com.xabarin.dao

import com.xabarin.score
import java.util.*
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.compareValuesBy

data class Box(val environment: String, val box: String, var score: Double = 0.0) : Comparable<Box> {
    override fun compareTo(other: Box) = compareValuesBy(this, other, {it.environment}, {it.box}, {it.score})

}

fun NavigableSet<Box>.complete(input: String, isBox: Boolean = true) : List<String> {

    //val list: List<String> = map { it.box + " " + it.score.toString()}

    if (input == "") return listOf("")

    if (isBox) {
        forEach { env ->
            env.score = env.box.score(input)
        }
    } else {
        forEach { env ->
            env.score = env.environment.score(input)
        }
    }


    // sortedWith(compareBy({it.score}))

    return filter { box -> box.score != 0.0}.sortedWith(compareByDescending({it.score})).map { it.box + " " + it.environment}
}
