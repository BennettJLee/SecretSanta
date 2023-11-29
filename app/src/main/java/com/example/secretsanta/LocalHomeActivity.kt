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
import com.example.secretsanta.lists.Gifting
import com.example.secretsanta.lists.GiftingListSingleton
import com.example.secretsanta.lists.Person
import com.example.secretsanta.lists.PersonListSingleton
import com.example.secretsanta.lists.RoomListSingleton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

class LocalHomeActivity : AppCompatActivity() {

    // SharedPreferences
    private lateinit var sharedPreferences : LocalSharedPreferences

    // recyclerView
    private lateinit var binding : ActivityLocalHomeBinding
    private lateinit var personAdapter: PersonAdapter

    // Room dropdown
    private lateinit var roomListAutoCompleteTextView : AutoCompleteTextView
    private lateinit var roomListTextInputLayout : TextInputLayout

    // Settings dropdown
    private lateinit var settingsArray : Array<String>
    private lateinit var settingsAutoCompleteTextView: AutoCompleteTextView
    private lateinit var settingsTextInputLayout : TextInputLayout

    // UI
    private lateinit var addFAB : FloatingActionButton

    // Class level
    private lateinit var currentRoom : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the sharedPreferences
        sharedPreferences = LocalSharedPreferences(this)

        // Set the settings dropdown variables
        settingsArray = resources.getStringArray(R.array.settings_values)
        settingsAutoCompleteTextView = findViewById(R.id.settingsAutoCompleteTextView)
        settingsTextInputLayout = findViewById(R.id.settingsTextInputLayout)

        // Set the room dropdown variables
        roomListAutoCompleteTextView = findViewById(R.id.roomListAutoCompleteTextView)
        roomListTextInputLayout = findViewById(R.id.roomListTextInputLayout)

        // Set the UI
        addFAB = findViewById(R.id.floatingActionButton)

        // Set the current room
        if (RoomListSingleton.roomList.isNotEmpty()) {
            currentRoom = sharedPreferences.loadCurrentRoomPref()
            roomListAutoCompleteTextView.setText(currentRoom)

            refreshRoomDropDown()
        }

        //load gifting and person list for current room and update the recycler
        GiftingListSingleton.giftingList = sharedPreferences.loadGiftingListPref(currentRoom)
        PersonListSingleton.personList = sharedPreferences.loadPersonListPref(currentRoom)
        updatePersonListView()

        // Set settings adapter
        val settingsAdapter = ArrayAdapter(this, R.layout.dropdown_item, settingsArray)
        settingsAutoCompleteTextView.setAdapter(settingsAdapter)

        // Set room list click listener
        roomListAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            closeKeyboard(roomListTextInputLayout)

            // Set the current room and corresponding variables
            val selectedItem = roomListAutoCompleteTextView.adapter.getItem(position).toString()
            currentRoom = selectedItem
            roomListTextInputLayout.editText?.setText(currentRoom)
            sharedPreferences.saveCurrentRoomPref(currentRoom)

            // clear the lists
            PersonListSingleton.personList.clear()
            GiftingListSingleton.giftingList.clear()

            // Set up the lists and update the listView
            GiftingListSingleton.giftingList = sharedPreferences.loadGiftingListPref(currentRoom)
            PersonListSingleton.personList = sharedPreferences.loadPersonListPref(currentRoom)
            updatePersonListView()

            // Adjust text view variables
            roomListAutoCompleteTextView.threshold = 0
            roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL

            refreshRoomDropDown()
        }

        // Set settings click listener
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
                    if (!PersonListSingleton.personList.contains(Person(""))) {
                        drawNames()
                    }
                }
            }
        }

        // set fab on click listener
        addFAB.setOnClickListener {

            closeKeyboard(addFAB)

            addPerson()
        }

    }

    /**
     * This function updates the person listview so all the people are present
     */
    private fun updatePersonListView() {

        // Set the adapter
        personAdapter = PersonAdapter(this, currentRoom, PersonListSingleton.personList)
        binding.personRecyclerView.adapter = personAdapter

        // Set the layout Manager
        val layoutManager = LinearLayoutManager(this)
        binding.personRecyclerView.layoutManager = layoutManager

        // Add padding to the view so the last item can be scrolled to the top
        binding.personRecyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.personRecyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                // Get first item for calculations
                val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstItemView = layoutManager.findViewByPosition(firstItemPosition)

                if (firstItemPosition == 0 && firstItemView != null) {

                    // Get the variables for the calculation
                    val recyclerViewHeight = binding.personRecyclerView.height
                    val firstItemHeight = firstItemView.height

                    // Calculate padding
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

        // Check if adding a person should be available
        if (!PersonListSingleton.personList.contains(Person("")) && currentRoom.isNotBlank()) {

            // add a new person and update the list
            PersonListSingleton.personList.add(Person(""))
            updatePersonListView()
       }

    }

    /**
     * This function adds a room to the room list and makes the room list textview editable
     */
    private fun addRoom(){

        // Set the text views variables and request focus
        roomListAutoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
        roomListAutoCompleteTextView.setText("")
        roomListAutoCompleteTextView.hint = "Enter Name..."
        roomListAutoCompleteTextView.threshold = 100
        roomListAutoCompleteTextView.requestFocus()

        //clear the current room, person list and update the listView and dropdown
        currentRoom = ""
        PersonListSingleton.personList.clear()
        updatePersonListView()
        refreshRoomDropDown()

        // Show the keyboard
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(roomListAutoCompleteTextView, InputMethodManager.SHOW_IMPLICIT)

        // Set an edit text listener
        roomListAutoCompleteTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // get the enteredText and capitalise it
                val enteredText = roomListAutoCompleteTextView.text.toString().lowercase().replaceFirstChar { it.uppercaseChar() }

                // Check the room doesn't already exist
                if (!RoomListSingleton.roomList.contains(enteredText) && enteredText.isNotEmpty()) {

                    // Set the current room and adjust variables to match
                    currentRoom = enteredText
                    roomListTextInputLayout.editText?.setText(currentRoom)
                    sharedPreferences.saveCurrentRoomPref(currentRoom)
                    RoomListSingleton.roomList.add(currentRoom)
                    sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)

                    // Clear the lists
                    PersonListSingleton.personList.clear()
                    GiftingListSingleton.giftingList.clear()

                    // Set up the lists and update the listView and dropdown
                    GiftingListSingleton.giftingList = sharedPreferences.loadGiftingListPref(currentRoom)
                    PersonListSingleton.personList = sharedPreferences.loadPersonListPref(currentRoom)
                    updatePersonListView()
                    refreshRoomDropDown()

                    // Adjust text view variables
                    roomListAutoCompleteTextView.threshold = 0
                    roomListAutoCompleteTextView.inputType = InputType.TYPE_NULL
                } else {

                    // If not then check if the text is not empty
                    if (enteredText.isNotEmpty()){
                        // If the text is not empty it must already exist so inform the user
                        roomListAutoCompleteTextView.text.clear()
                        Toast.makeText(this, "This room already exists", Toast.LENGTH_SHORT).show()
                    }
                }

                closeKeyboard(roomListAutoCompleteTextView)

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

        // If the roomList is not empty set the room to the next in the list
        // and adjust any variables to match the current room
        if (RoomListSingleton.roomList.isNotEmpty()){

            roomListAutoCompleteTextView.setText(RoomListSingleton.roomList[0])
            currentRoom = RoomListSingleton.roomList[0]
            sharedPreferences.saveCurrentRoomPref(currentRoom)

            PersonListSingleton.personList.clear()
            GiftingListSingleton.giftingList.clear()

            //load the person list and update the listView
            GiftingListSingleton.giftingList = sharedPreferences.loadGiftingListPref(currentRoom)
            PersonListSingleton.personList = sharedPreferences.loadPersonListPref(currentRoom)
            updatePersonListView()
        } else {

            // Otherwise clear lists and start the LaunchActivity
            PersonListSingleton.personList.clear()
            GiftingListSingleton.giftingList.clear()
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
    private fun drawNames(){

        GiftingListSingleton.giftingList.clear()

        //make 2 copies the list and shuffle the second list
        var giftList: MutableList<Person> = PersonListSingleton.personList.toMutableList()
        var receiveList: MutableList<Person> = PersonListSingleton.personList.toMutableList()
        receiveList.shuffle()

        //Set up gifting list and matched lists
        val giftingList: MutableList<Gifting> = mutableListOf()
        val matchedGifterList: MutableList<Person> = mutableListOf()
        val matchedReceiverList: MutableList<Person> = mutableListOf()

        var index = 0
        //match across lists until everyone has been matches
        while (index < giftList.size) {

            var receiverFound = false
            val gifter = giftList[index]

            // Loop receivers to find a match
            for (receiver in receiveList) {

                // If the gifter and receiver aren't the same person and
                // receiver hasn't already been matched, then match
                if (gifter != receiver && !matchedReceiverList.contains(receiver)) {

                    // Add the match to appropriate lists and mark as found
                    giftingList.add(Gifting(gifter, receiver))
                    matchedGifterList.add(gifter)
                    matchedReceiverList.add(receiver)
                    receiverFound = true
                    break
                }
            }
            index++

            //if receiver was not found, remove the last match and rematch
            if (!receiverFound) {
                //remove the last match
                giftingList.removeAt(giftingList.size-1)
                matchedGifterList.removeAt(matchedGifterList.size-1)
                matchedReceiverList.removeAt(matchedReceiverList.size-1)

                //filter out already matched people
                receiveList = receiveList.filter { it !in matchedReceiverList }.toMutableList()
                giftList = giftList.filter { it !in matchedGifterList }.toMutableList()
                receiveList.shuffle()

                index = 0
            }
        }

        // Set the giftingList and save
        GiftingListSingleton.giftingList = giftingList
        sharedPreferences.saveGiftingListPref(currentRoom, GiftingListSingleton.giftingList)
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


    /**
     * This function closes the keyboard
     */
    private fun closeKeyboard(view: View){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}