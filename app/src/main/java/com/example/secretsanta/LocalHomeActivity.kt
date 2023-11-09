package com.example.secretsanta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class LocalHomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : LocalSharedPreferences

    private var personList: MutableList<Person> = mutableListOf()

    // ui
    private lateinit var roomListAutoCompleteTextView : AutoCompleteTextView
    private lateinit var roomListTextInputLayout : TextInputLayout

    private lateinit var settingsArray : Array<String>
    private lateinit var settingsAutoCompleteTextView: AutoCompleteTextView
    private lateinit var settingsTextInputLayout : TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_home)

        sharedPreferences = LocalSharedPreferences(this)

        settingsArray = resources.getStringArray(R.array.settings_values)
        settingsAutoCompleteTextView = findViewById(R.id.settingsAutoCompleteTextView)
        settingsTextInputLayout = findViewById(R.id.settingsTextInputLayout)

        roomListAutoCompleteTextView = findViewById(R.id.roomListAutoCompleteTextView)
        roomListTextInputLayout = findViewById(R.id.roomListTextInputLayout)

        //** WARNING : Changes need to be made here
        if (RoomListSingleton.roomList.isNotEmpty()) {
            roomListAutoCompleteTextView.setText(sharedPreferences.loadCurrentRoomPref())

            refreshRoomDropDown()
        }

        val settingsAdapter = ArrayAdapter(this, R.layout.dropdown_item, settingsArray)
        settingsAutoCompleteTextView.setAdapter(settingsAdapter)


        roomListAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            val selectedItem = roomListAutoCompleteTextView.adapter.getItem(position).toString()
            roomListTextInputLayout.editText?.setText(selectedItem)

            sharedPreferences.saveCurrentRoomPref(selectedItem)
            roomListAutoCompleteTextView.threshold = 0
            roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL

            refreshRoomDropDown()
        }

        settingsAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            if (position == 0){
                addRoom()
            } else if (position == 1) {
                removeRoom()
            }

        }



        //** TESTING : Below is for testing purposes. Load and then delete sortlist to test persistance **//

        personList.add(Person("Ben", "123"))
        personList.add(Person("Sam", "123"))
        personList.add(Person("Jakob", "123"))

        sortGifting(personList)

        val list = sharedPreferences.loadGiftingListPref("Family")

        Log.e("TEST", list.toString())

        //sharedPreferences.clearRoomNamesPref()
    }

    /**
     * This function adds a room to the room list and makes the room list textview editable
     */
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
                // get the enteredText and capitalise it
                val enteredText = roomListAutoCompleteTextView.text.toString().replaceFirstChar { it.uppercaseChar() }

                if (!RoomListSingleton.roomList.contains(enteredText) && enteredText.isNotEmpty()) {

                    RoomListSingleton.roomList.add(enteredText)
                    sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)

                    roomListTextInputLayout.editText?.setText(enteredText)
                    sharedPreferences.saveCurrentRoomPref(enteredText)

                    refreshRoomDropDown()
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

    /**
     * Remove a room from the list of room names
     *
     * @param roomName the name of the room
     */
    private fun removeRoom() {
        RoomListSingleton.roomList.remove(roomListAutoCompleteTextView.text.toString())
        sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)

        if (RoomListSingleton.roomList.isNotEmpty()){
            roomListAutoCompleteTextView.setText(RoomListSingleton.roomList[0])
            sharedPreferences.saveCurrentRoomPref(RoomListSingleton.roomList[0])
        } else {
            val intent = Intent(this, LaunchActivity::class.java)
            startActivity(intent)
        }

        refreshRoomDropDown()
    }

    /**
     * This function will duplicate the list and match a gifter with a receiver.
     *
     * @param giftList The list of people in the room
     */
    private fun sortGifting(giftList: MutableList<Person>) : List<Gifting>{

        val giftingList = mutableListOf<Gifting>()

        //copy the list and shuffle the copied list
        val receiveList: MutableList<Person> = giftList.toMutableList()
        receiveList.shuffle()

        //match across lists until both are empty
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

        return giftingList
    }

    /**
     * This function refreshes the room list dropdown but resetting the adapter
     */
    private fun refreshRoomDropDown(){
        // Fill the dropdown with rooms expect the current room
        val adjustedRoomList = RoomListSingleton.roomList.toMutableList()
        adjustedRoomList.remove(roomListAutoCompleteTextView.text.toString())
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, adjustedRoomList)
        roomListAutoCompleteTextView.setAdapter(adapter)
    }
}