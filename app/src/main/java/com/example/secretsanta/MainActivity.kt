package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.PopupWindow

class MainActivity : AppCompatActivity() {

    private lateinit var giftingData: GiftingData

    private var roomNames : MutableList<String> = mutableListOf()
    private var personList: MutableList<Person> = mutableListOf()
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)
        loadRoomNamesPref()
        setContentView(R.layout.activity_main)

        giftingData = GiftingData(this)

        //** TESTING : Below is for testing purposes. Load and then delete sortlist to test persistance **//

        personList.add(Person("Ben", "123"))
        personList.add(Person("Jakob", "234"))
        personList.add(Person("Sam", "345"))

        giftingData.sortList("Family", personList)

        val list = giftingData.loadGiftingList("Family")

        Log.e("TEST", list.toString())
    }

    //** WARNING : This may change as we can add a text listener to the room name text box. **//
    /**
     * Add a room to the list of room names
     *
     * @param roomName the name of the room
     */
    fun saveRoom(roomName: String){
        roomNames.add(roomName)
        saveRoomNamesPref(roomNames)
    }

    fun addRoom(){
        /**roomNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                roomNameChanged = true

            }
        }) **/
    }

    /**
     * Delete a room from the list of room names
     *
     * @param roomName the name of the room
     */
    fun deleteRoom(roomName : String){
        if(roomNames.contains(roomName)){
            roomNames.remove(roomName)
            saveRoomNamesPref(roomNames)
        }
    }

    /**
     * Load a list of room names from sharedPreferences.
     *
     * @return return a list of room names
     */
    private fun loadRoomNamesPref(){
        if (sharedPreferences.contains("RoomNames")){
            val stringSet = sharedPreferences.getStringSet("RoomNames", setOf())
            if (stringSet != null) {
                roomNames = stringSet.toMutableList()
            }
        }
        // else change state of program to create/join screen
    }

    /**
     * Save the room names to sharedPreferences.
     *
     * @param roomNames the list of room names
     */
    private fun saveRoomNamesPref(roomNames : List<String>){
        val stringSet = roomNames.toSet()
        sharedPreferences.edit().putStringSet("RoomNames", stringSet).apply()
    }
}