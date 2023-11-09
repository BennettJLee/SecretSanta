package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalSharedPreferences(context: Context) : AppCompatActivity() {

    private var sharedPreferences : SharedPreferences =
        context.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)


    /**
     * This function will load the gifting list from sharedPreferences
     *
     * @param roomName The name of the room
     */
    fun loadGiftingListPref(roomName : String) : MutableList<Gifting>{

        var giftingListPref = mutableListOf<Gifting>()

        // check if sharedPreferences has the current room name, if not, don't retrieve anything
        if (sharedPreferences.contains(roomName)){
            val json = sharedPreferences.getString(roomName, null)

            // Get the json and convert it into a list
            val gson = Gson()
            val giftList = object : TypeToken<List<Gifting>>() {}.type
            giftingListPref = gson.fromJson(json, giftList)
        }
        return giftingListPref
    }

    /**
     * Save the gifting list to sharedPreferences.
     *
     * @param roomName the name of the room
     * @param giftingList the gifting list to be saved
     */
    fun saveGiftingListPref(roomName : String, giftingList : MutableList<Gifting>){
        val gson = Gson()
        val giftingJson = gson.toJson(giftingList)

        sharedPreferences.edit().putString(roomName, giftingJson).apply()
    }

    /**
     * Load a list of room names from sharedPreferences.
     *
     * @return return a list of room names
     */
    fun loadRoomNamesPref() : Boolean{
        var inRoom = false
        if (sharedPreferences.contains("RoomNames")){
            val stringSet = sharedPreferences.getStringSet("RoomNames", setOf())
            if (!stringSet.isNullOrEmpty()) {
                RoomListSingleton.roomList = stringSet.toMutableList()
                inRoom = true
            }
        }
        return inRoom
    }

    /**
     * Save the current room to sharedPreferences.
     *
     * @param currentRoom the name of the current room
     */
    fun saveCurrentRoomPref(currentRoom : String){
        sharedPreferences.edit().putString("currentRoom", currentRoom).apply()
    }

    /**
     * Load the current room the user is in from sharedPreferences.
     *
     * @return return the current room name
     */
    fun loadCurrentRoomPref() : String{
        var currentRoom = ""
        if (sharedPreferences.contains("currentRoom")){
            currentRoom = sharedPreferences.getString("currentRoom", "").toString()
        }
        return currentRoom
    }

    /**
     * Save the room names to sharedPreferences.
     *
     * @param roomNames the list of room names
     */
    fun saveRoomNamesPref(roomNames : List<String>){
        val stringSet = roomNames.toSet()
        sharedPreferences.edit().putStringSet("RoomNames", stringSet).apply()
    }

    fun clearRoomNamesPref(){
        sharedPreferences.edit().remove("RoomNames").apply()
    }
}
