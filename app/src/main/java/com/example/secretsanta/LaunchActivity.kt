package com.example.secretsanta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import com.example.secretsanta.lists.RoomListSingleton

class LaunchActivity : AppCompatActivity() {

    // shared preferences
    private lateinit var sharedPreferences : LocalSharedPreferences

    // UI
    private lateinit var createRoomButton : Button
    private lateinit var createRoomText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        // Set shared preferences
        sharedPreferences = LocalSharedPreferences(this)

        // Set UI
        createRoomButton = findViewById(R.id.createRoomButton)
        createRoomText = findViewById(R.id.createRoomText)

        // Set create room click listener for creating a room
        createRoomButton.setOnClickListener {
            createRoom(createRoomText)
        }

    }

    /**
     * This function creates a room and launches the local home activity
     *
     * @param editText The text view that the user will be editing
     */
    private fun createRoom(editText : TextView){

        // Adjust edit text variables and request focus
        editText.visibility = View.VISIBLE
        editText.isEnabled = true
        editText.requestFocus()

        // Show keyboard
        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        // set a edit text listener on the edit text
        editText.setOnEditorActionListener { v, actionId, _ ->

            // If the text is confirmed
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                // Get the enteredText and capitalise it
                val enteredText = editText.text.toString().lowercase().replaceFirstChar { it.uppercaseChar() }

                // Hide keyboard
                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                // Check is the enteredText is not empty
                if (enteredText.isNotEmpty()){

                    //if so, add the entered to to the roomList and save
                    RoomListSingleton.roomList.add(enteredText)
                    sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)
                    sharedPreferences.saveCurrentRoomPref(enteredText)

                    // Remove listener and start the LocalHomeActivity
                    editText.setOnEditorActionListener(null)
                    val intent = Intent(this, LocalHomeActivity::class.java)
                    startActivity(intent)
                } else {

                    // If not Adjust edit text variables
                    editText.visibility = View.INVISIBLE
                    editText.isEnabled = false
                }

                true
            } else {
                false
            }
        }
    }

}