package com.example.secretsanta

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import com.example.secretsanta.databinding.PersonWindowBinding
import com.example.secretsanta.lists.GiftingListSingleton

class PersonWindow(private val context: Context) {

    // Binding
    private lateinit var binding: PersonWindowBinding

    // Popup dialog
    private lateinit var dialog: Dialog

    /**
     * This function shows the the person dialog window
     */
    fun showPersonWindow(personName: String) {

        // Initialise dialog
        dialog = Dialog(context)
        binding = PersonWindowBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // Set UI
        val personNameText = binding.personNameText
        val drawnNameText: TextView = binding.drawnNameText
        personNameText.text = personName

        // Check if a gifting list exists
        if (GiftingListSingleton.giftingList.isNotEmpty()) {

            // If so, find the person
            val gifting = GiftingListSingleton.giftingList.find { it.gifter.name == personName }

            // If gifting exists then set UI
            if (gifting != null) {
                drawnNameText.text = gifting.receiver.name
            }
        }

        // Clear the background and show
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }
}