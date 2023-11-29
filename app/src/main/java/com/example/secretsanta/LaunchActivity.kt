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

    private lateinit var sharedPreferences : LocalSharedPreferences

    // ui
    private lateinit var createRoomButton : Button
    private lateinit var createRoomText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        sharedPreferences = LocalSharedPreferences(this)

        createRoomButton = findViewById(R.id.createRoomButton)
        createRoomText = findViewById(R.id.createRoomText)

        createRoomButton.setOnClickListener {
            createRoom(createRoomText)
        }

    }

    /**
     * This function creates a room and launches the local home activity
     */
    private fun createRoom(editText : TextView){

        editText.visibility = View.VISIBLE
        editText.isEnabled = true
        editText.requestFocus()

        var inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // get the enteredText and capitalise it
                val enteredText = editText.text.toString().replaceFirstChar { it.uppercaseChar() }

                inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                // if the text is not empty, save the name and launch home screen
                // otherwise, remove the edit text from the screen
                if (enteredText.isNotEmpty()){
                    RoomListSingleton.roomList.add(enteredText)
                    sharedPreferences.saveRoomNamesPref(RoomListSingleton.roomList)
                    sharedPreferences.saveCurrentRoomPref(enteredText)

                    editText.setOnEditorActionListener(null)
                    val intent = Intent(this, LocalHomeActivity::class.java)
                    startActivity(intent)
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

}