package com.aqua.fill.aquaalarm.models

import java.io.Serializable

data class WaterDataModel(
    val day: Int,
    val month:Int,
    val year:Int
) : Serializable
