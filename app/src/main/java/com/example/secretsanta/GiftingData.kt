package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class GiftingData(private val context: Context) {

    private var giftingList: MutableList<Gifting> = mutableListOf()
    private val sharedPreferences : SharedPreferences = context.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)


    /**
     * This function will duplicate the list and match gifter with receiver.
     *
     * @param roomName The name of the room
     * @param giftList The list of people in the room
     */
    private fun sortList(roomName : String, giftList: List<Person>){

        //copy the list and shuffle the copied list
        giftList.toMutableList()
        val receiveList = giftList.toMutableList()
        receiveList.shuffle()

        while (giftList.isNotEmpty()){
            //if the name is the same shuffle and try again
            if (giftList[0].name == receiveList[0].name){
                giftingList.add(Gifting(giftList[0], receiveList[1]))
            } else {
                giftingList.add(Gifting(giftList[0], receiveList[0]))
            }
        }

        //** WARNING : This is theoretical, I have no idea if it will work
        val gson = Gson()
        // Serialize the list to a JSON string
        val giftingJson = gson.toJson(giftingList)

        sharedPreferences.edit().putString(roomName, giftingJson).apply()
    }

    fun loadGiftingListPref(){

    }
}

data class Person(
    val name : String,
    val pNum : String
)

data class Gifting(
    val gifter : Person,
    val receiver : Person
)