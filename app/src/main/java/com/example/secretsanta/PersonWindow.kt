package com.example.secretsanta

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.secretsanta.databinding.PersonWindowBinding
import com.example.secretsanta.lists.GiftingListSingleton

class PersonWindow(private val context: Context) {

    private lateinit var binding: PersonWindowBinding
    private lateinit var dialog: Dialog

    fun showPersonWindow(personName: String) {

        dialog = Dialog(context)
        binding = PersonWindowBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        val personNameText = binding.personNameText
        val drawnNameText: TextView = binding.drawnNameText

        personNameText.text = personName

        if (GiftingListSingleton.giftingList.isNotEmpty()) {
            val gifting = GiftingListSingleton.giftingList.find { it.gifter.name == personName }
            if (gifting != null) {
                drawnNameText.text = gifting.receiver.name
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    /**
     * This function checks if the window is showing or initialized
     *
     * @return The boolean value of weather the popup window is showing
     */
    fun isShowing(): Boolean {

        return this::dialog.isInitialized && dialog.isShowing
    }

    /**
     * This function closes the promotion window
     */
    fun closePromotionWindow() {

        if (isShowing()) {

            dialog.dismiss()
        }
    }
}