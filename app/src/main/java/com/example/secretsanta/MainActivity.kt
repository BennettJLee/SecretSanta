package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var giftingData: GiftingData

    private var roomNames : MutableList<String> = mutableListOf()
    private var personList: MutableList<Person> = mutableListOf()
    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var roomNameText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)
        loadRoomNamesPref()
        setContentView(R.layout.activity_main)

        giftingData = GiftingData(this)


        val createRoomButton = findViewById<Button>(R.id.createRoomButton)
        roomNameText = findViewById<TextView>(R.id.roomNameText)


        createRoomButton.setOnClickListener {
            createRoom()
        }

        if(roomNames.isNotEmpty()){
            roomNameText = findViewById<TextView>(R.id.roomNameText)
            roomNameText.text = roomNames[0]
        }

        //** TESTING : Below is for testing purposes. Load and then delete sortlist to test persistance **//

        Log.e("TEST", roomNames.toString())

        personList.add(Person("Ben", "123"))
        personList.add(Person("Jakob", "234"))
        personList.add(Person("Sam", "345"))

        giftingData.sortList("Family", personList)

        val list = giftingData.loadGiftingList("Family")

        Log.e("TEST", list.toString())

        //clearRoomNamesPref()

    }

    //** WARNING : This may change as we can add a text listener to the room name text box. **//
    /**
     * Add a room to the list of room names
     *
     * @param roomName the name of the room
     */
    private fun saveRoom(roomName: String){
        roomNames.add(roomName)
        saveRoomNamesPref(roomNames)
    }

    private fun createRoom(){

        roomNameText.requestFocus()

        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(roomNameText, InputMethodManager.SHOW_IMPLICIT)

        //val roomNameChanged = false

        roomNameText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val enteredText = roomNameText.text.toString()
                roomNames.add(enteredText)
                saveRoomNamesPref(roomNames)

                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                Log.e("TEST", "1")
                true
            } else {
                Log.e("TEST", "2")
                false
            }
        }
        Log.e("TEST", roomNames.toString())
    }

    /**
     * Delete a room from the list of room names
     *
     * @param roomName the name of the room
     */
    private fun deleteRoom(roomName : String){
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

    private fun clearRoomNamesPref(){
        sharedPreferences.edit().remove("RoomNames").apply()
    }
}