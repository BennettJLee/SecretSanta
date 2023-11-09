package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var giftingData: GiftingData

    private var roomNames : MutableList<String> = mutableListOf()
    private var personList: MutableList<Person> = mutableListOf()
    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var roomNameText : TextView
    private lateinit var addRoomButton : Button
    private lateinit var roomListAutoCompleteTextView : AutoCompleteTextView
    private lateinit var roomListTextInputLayout : TextInputLayout

    private lateinit var settingsArray : Array<String>
    private lateinit var settingsAutoCompleteTextView: AutoCompleteTextView
    private lateinit var settingsTextInputLayout : TextInputLayout

    private lateinit var createRoomButton : Button
    private lateinit var createRoomText : TextView

    private var inRoom : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("SecretSantaPreferences", Context.MODE_PRIVATE)
        loadRoomNamesPref()

        if(inRoom){
            launchHomeView()
        } else {
            launchJoinCreateView()
        }
    }

    private fun launchHomeView(){
        setContentView(R.layout.activity_main)

        giftingData = GiftingData(this)

        settingsArray = resources.getStringArray(R.array.settings_values)
        settingsAutoCompleteTextView = findViewById(R.id.settingsAutoCompleteTextView)
        settingsTextInputLayout = findViewById(R.id.settingsTextInputLayout)

        roomListAutoCompleteTextView = findViewById(R.id.roomListAutoCompleteTextView)
        roomListTextInputLayout = findViewById(R.id.roomListTextInputLayout)


        val roomListAdapter = ArrayAdapter(this, R.layout.dropdown_item, roomNames)
        roomListAutoCompleteTextView.setAdapter(roomListAdapter)

        val settingsAdapter = ArrayAdapter(this, R.layout.dropdown_item, settingsArray)
        settingsAutoCompleteTextView.setAdapter(settingsAdapter)


        roomListAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = roomNames[position]
            roomListTextInputLayout.editText?.setText(selectedItem)

            //** WARNING : Changes need to be made here. Maybe a method
            val roomListAdapter = ArrayAdapter(this, R.layout.dropdown_item, roomNames)

            roomListAutoCompleteTextView.setAdapter(roomListAdapter)
        }

        settingsAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            if (position == 0){
                addRoom()
            } else if (position == 1) {
                removeRoom()
            }

        }

        //** WARNING : Changes need to be made here
        if (roomNames.isNotEmpty()) {

            roomListAutoCompleteTextView.setText(roomNames[0])
            val adapter = ArrayAdapter(this, R.layout.dropdown_item, roomNames)

            roomListAutoCompleteTextView.setAdapter(adapter)
        }

        //** TESTING : Below is for testing purposes. Load and then delete sortlist to test persistance **//

        Log.e("TEST", roomNames.toString())

        personList.add(Person("Ben", "123"))
        personList.add(Person("Sam", "123"))
        personList.add(Person("Jakob", "123"))

        giftingData.sortList("Family", personList)

        val list = giftingData.loadGiftingList("Family")

        Log.e("TEST", list.toString())

        //clearRoomNamesPref()
    }

    private fun launchJoinCreateView(){
        setContentView(R.layout.activity_joincreate)

        createRoomButton = findViewById(R.id.createRoomButton)

        createRoomText = findViewById(R.id.createRoomText)

        createRoomButton.setOnClickListener {
            createRoom(createRoomText)

        }

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

    private fun addRoom(){

        roomListAutoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT

        roomListAutoCompleteTextView.setText("")
        roomListAutoCompleteTextView.hint = "Enter Name..."
        roomListAutoCompleteTextView.threshold = 100

        roomListAutoCompleteTextView.requestFocus()

        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(roomListAutoCompleteTextView, InputMethodManager.SHOW_IMPLICIT)

        roomListAutoCompleteTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val enteredText = roomListAutoCompleteTextView.text.toString().replaceFirstChar { it.uppercaseChar() }

                if (!roomNames.contains(enteredText)) {

                    roomNames.add(enteredText)
                    saveRoomNamesPref(roomNames)

                    roomListTextInputLayout.editText?.setText(enteredText)

                    //** WARNING : Changes need to be made here. Maybe a method
                    val adapter = ArrayAdapter(this, R.layout.dropdown_item, roomNames)
                    roomListAutoCompleteTextView.setAdapter(adapter)
                }

                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                roomListAutoCompleteTextView.threshold = 0
                roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL
                true
            } else {
                false
            }
        }
    }

    private fun removeRoom() {

    }

    private fun createRoom(editText : TextView){

        editText.visibility = View.VISIBLE
        editText.isEnabled = true
        editText.requestFocus()

        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val enteredText = editText.text.toString().replaceFirstChar { it.uppercaseChar() }

                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                // if the text is not empty, save the name and launch home screen
                // otherwise, remove the edit text from the screen
                if (enteredText.isNotEmpty()){
                    roomNames.add(enteredText)
                    saveRoomNamesPref(roomNames)

                    editText.setOnEditorActionListener(null)
                    launchHomeView()
                } else {
                    editText.visibility = View.INVISIBLE
                    editText.isEnabled = false
                }

                true
            } else {
                false
            }
        }
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
                inRoom = true
            }
        } else {
            inRoom = false
        }
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