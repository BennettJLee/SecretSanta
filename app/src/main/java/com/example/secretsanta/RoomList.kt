package com.example.secretsanta

object RoomListSingleton {
    var roomList : MutableList<String> = mutableListOf()
}

data class Person(
    val name : String,
    val pNum : String
)

data class Gifting(
    val gifter : Person,
    val receiver : Person
)