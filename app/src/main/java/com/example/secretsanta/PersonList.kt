package com.example.secretsanta

object PersonListSingleton {
    var personList : MutableList<Person> = mutableListOf()
}

data class Person(
    var name : String
)

data class Gifting(
    val gifter : Person,
    val receiver : Person
)