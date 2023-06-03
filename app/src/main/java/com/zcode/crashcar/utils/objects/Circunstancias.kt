package com.zcode.crashcar.utils.objects

/*
 *    Created by Nono on 02/06/2023.
 */
class Circunstancias : ArrayList<Circunstancia>()

data class Circunstancia(
    val id: String,
    val check: Boolean
)