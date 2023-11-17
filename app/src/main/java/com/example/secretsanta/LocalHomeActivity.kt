package com.example.secretsanta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.secretsanta.databinding.ActivityLocalHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

class LocalHomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : LocalSharedPreferences

    private lateinit var binding : ActivityLocalHomeBinding
    private lateinit var personAdapter: PersonAdapter

    // ui
    private lateinit var roomListAutoCompleteTextView : AutoCompleteTextView
    private lateinit var roomListTextInputLayout : TextInputLayout
    private lateinit var addFAB : FloatingActionButton

    private lateinit var settingsArray : Array<String>
    private lateinit var settingsAutoCompleteTextView: AutoCompleteTextView
    private lateinit var settingsTextInputLayout : TextInputLayout

    private lateinit var currentRoom : String
    private lateinit var giftingList: MutableList<Gifting>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = LocalSharedPreferences(this)

        settingsArray = resources.getStringArray(R.array.settings_values)
        settingsAutoCompleteTextView = findViewById(R.id.settingsAutoCompleteTextView)
        settingsTextInputLayout = findViewById(R.id.settingsTextInputLayout)

        roomListAutoCompleteTextView = findViewById(R.id.roomListAutoCompleteTextView)
        roomListTextInputLayout = findViewById(R.id.roomListTextInputLayout)

        addFAB = findViewById(R.id.floatingActionButton)


        if (RoomListSingleton.roomList.isNotEmpty()) {
            currentRoom = sharedPreferences.loadCurrentRoomPref()
            roomListAutoCompleteTextView.setText(currentRoom)

            refreshRoomDropDown()
        }

        //load gifting and person list for current room
        giftingList = sharedPreferences.loadGiftingListPref(currentRoom)
        sharedPreferences.loadPersonListPref(currentRoom)


        updatePersonListView()


        val settingsAdapter = ArrayAdapter(this, R.layout.dropdown_item, settingsArray)
        settingsAutoCompleteTextView.setAdapter(settingsAdapter)


        roomListAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            closeKeyboard(roomListTextInputLayout)

            val selectedItem = roomListAutoCompleteTextView.adapter.getItem(position).toString()
            roomListTextInputLayout.editText?.setText(selectedItem)

            currentRoom = selectedItem
            sharedPreferences.saveCurrentRoomPref(currentRoom)

            sharedPreferences.loadPersonListPref(currentRoom)
            updatePersonListView()

            roomListAutoCompleteTextView.threshold = 0
            roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL

            refreshRoomDropDown()
        }

        settingsAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            closeKeyboard(settingsAutoCompleteTextView)

            when (position) {
                0 -> {
                    addRoom()
                }
                1 -> {
                    removeRoom()
                }
                2 -> {
                    drawNames()
                }
            }
        }

        addFAB.setOnClickListener {

            closeKeyboard(addFAB)

            addPerson()
        }

    }


    private fun updatePersonListView() {
        //need to save a new list
        personAdapter = PersonAdapter(this, currentRoom, PersonListSingleton.personList)

        binding.personRecyclerView.adapter = personAdapter
        val layoutManager = LinearLayoutManager(this)
        binding.personRecyclerView.layoutManager = layoutManager

        binding.personRecyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.personRecyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstItemView = layoutManager.findViewByPosition(firstItemPosition)

                if (firstItemPosition == 0 && firstItemView != null) {
                    val recyclerViewHeight = binding.personRecyclerView.height
                    val firstItemHeight = firstItemView.height

                    // Calculate padding to allow scrolling all the way up until the last item is visible
                    val padding = recyclerViewHeight - firstItemHeight
                    binding.personRecyclerView.setPadding(0, 0, 0, padding)
                }

                return true
            }
        })

    }

    /**
     * This function adds a person to the room
     */
    private fun addPerson(){

        if (!PersonListSingleton.personList.contains(Person("")) && currentRoom.isNotBlank()) {

            PersonListSingleton.personList.add(Person(""))

            personAdapter = PersonAdapter(this, currentRoom, PersonListSingleton.personList)
            binding.personRecyclerView.adapter = personAdapter
       }
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

        //clear the current room, person list and update the listView
        currentRoom = ""
        PersonListSingleton.personList.clear()
        updatePersonListView()

        refreshRoomDropDown()

        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(roomListAutoCompleteTextView, InputMethodManager.SHOW_IMPLICIT)

        roomListAutoCompleteTextView.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // get the enteredText and capitalise it
                val enteredText = roomListAutoCompleteTextView.text.toString().replaceFirstChar { it.uppercaseChar() }

                if (!RoomListSingleton.roomList.contains(enteredText) && enteredText.isNotEmpty()) {

                    RoomListSingleton.roomList.add(enteredText)
                    sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)

                    currentRoom = enteredText

                    roomListTextInputLayout.editText?.setText(currentRoom)
                    sharedPreferences.saveCurrentRoomPref(currentRoom)

                    sharedPreferences.loadPersonListPref(currentRoom)
                    updatePersonListView()
                    refreshRoomDropDown()

                    roomListAutoCompleteTextView.threshold = 0
                    roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL
                } else {
                    if (enteredText.isNotEmpty()){
                        roomListAutoCompleteTextView.text.clear()
                        Toast.makeText(this, "This room already exists", Toast.LENGTH_SHORT).show()
                    }
                }

                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                true
            } else {
                false
            }
        }
    }

    /**
     * Remove a room from the list of room names
     */
    private fun removeRoom() {
        //remove room and save the room list
        RoomListSingleton.roomList.remove(roomListAutoCompleteTextView.text.toString())
        sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)
        sharedPreferences.removeGiftingListPref(currentRoom)
        sharedPreferences.removePersonListPref(currentRoom)

        //clear the person list and update the listView
        PersonListSingleton.personList.clear()
        updatePersonListView()

        if (RoomListSingleton.roomList.isNotEmpty()){
            roomListAutoCompleteTextView.setText(RoomListSingleton.roomList[0])
            currentRoom = RoomListSingleton.roomList[0]
            sharedPreferences.saveCurrentRoomPref(currentRoom)

            //load the person list and update the listView
            sharedPreferences.loadPersonListPref(currentRoom)
            updatePersonListView()
        } else {
            val intent = Intent(this, LaunchActivity::class.java)
            startActivity(intent)
        }

        refreshRoomDropDown()
    }

    /**
     * This function will duplicate the list and draw names for gifting
     *
     * @return The list of drawn names
     */
    private fun drawNames() : List<Gifting>{

        val giftingList = mutableListOf<Gifting>()

        //copy the list and shuffle the copied list
        val receiveList: MutableList<Person> = PersonListSingleton.personList.toMutableList()
        receiveList.shuffle()

        //match across lists until both are empty
        while (PersonListSingleton.personList.isNotEmpty()) {
            val gifter = PersonListSingleton.personList.removeAt(0)

            for (receiver in receiveList){
                if(receiver.name != gifter.name){
                    giftingList.add(Gifting(gifter, receiver))
                    receiveList.remove(receiver)
                    break
                }
            }
        }

        sharedPreferences.saveGiftingListPref(currentRoom, giftingList)
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

    private fun closeKeyboard(view: View){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}