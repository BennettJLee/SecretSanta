package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GiftingData(private val context: Context) {

    private var giftingList: MutableList<Gifting> = mutableListOf()
    private val sharedPreferences : SharedPreferences = context.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)

    /**
     * This function loads the gifting list
     *
     * @param roomName The name of the room
     * @return The gifting list
     */
    fun loadGiftingList(roomName : String) : MutableList<Gifting>{

        if(giftingList.isNotEmpty()){
            return giftingList
        }
        giftingList = loadGiftingListPref(roomName)

        return giftingList
    }

    /**
     * This function will duplicate the list and match gifter with receiver.
     *
     * @param roomName The name of the room
     * @param giftList The list of people in the room
     */
    fun sortList(roomName : String, giftList: MutableList<Person>){

        //copy the list and shuffle the copied list
        var receiveList: MutableList<Person> = giftList.toMutableList()
        receiveList.shuffle()

        while (giftList.isNotEmpty()) {
            val gifter = giftList.removeAt(0)

            for (receiver in receiveList){
                if(receiver.name != gifter.name){
                    giftingList.add(Gifting(gifter, receiver))
                    receiveList.remove(receiver)
                    break
                }
            }
        }



        //** WARNING : This is theoretical, I have no idea if it will work **//
        val gson = Gson()
        // Serialize the list to a JSON string
        val giftingJson = gson.toJson(giftingList)

        sharedPreferences.edit().putString(roomName, giftingJson).apply()
    }

    /**
     * This function will load the gifting list from sharedPreferences
     *
     * @param roomName The name of the room
     */
    private fun loadGiftingListPref(roomName : String) : MutableList<Gifting>{

        var giftingListPref = mutableListOf<Gifting>()

        //** WARNING : This is theoretical, I have no idea if it will work **//
        if (sharedPreferences.contains(roomName)){
            val json = sharedPreferences.getString(roomName, null)

            val gson = Gson()
            val giftList = object : TypeToken<List<Gifting>>() {}.type
            giftingListPref = gson.fromJson(json, giftList)
        }
        return giftingListPref
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