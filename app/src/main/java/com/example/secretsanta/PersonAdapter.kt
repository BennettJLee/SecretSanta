package com.example.secretsanta

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.secretsanta.databinding.PersoncardItemBinding
import com.example.secretsanta.lists.GiftingListSingleton
import com.example.secretsanta.lists.Person
import com.example.secretsanta.lists.PersonListSingleton

class PersonAdapter(private val context: Context, private val currentRoom: String, private val personList: MutableList<Person>) :
RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    // Person window
    private lateinit var personWindow : PersonWindow

    // Inner class representing the view holder for each item in the RecyclerView
    inner class PersonViewHolder(binding: PersoncardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val personNameEditText: EditText = binding.personNameEditText
        val personRemoveButton: RelativeLayout = binding.removePersonButton
        val personDetailsButton: RelativeLayout = binding.personDetailsButton
    }

    // Creating the view holder by inflating the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val binding = PersoncardItemBinding.inflate(inflater, parent, false)
        return PersonViewHolder(binding)
    }

    // Binding the data to the view holder
    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {

        // Get the current person
        val currentPerson = personList[position]

        // Set person window
        personWindow = PersonWindow(context)

        // Set the UI and adjust variables
        holder.personNameEditText.setText(currentPerson.name)
        holder.personNameEditText.isFocusable = currentPerson.name.isEmpty()
        holder.personNameEditText.isClickable = currentPerson.name.isEmpty()

        // Check if the persons name is blank
        if (currentPerson.name.isEmpty()) {

            // If so, set a text edit listener for personNameEditText to change the name
            holder.personNameEditText.setOnEditorActionListener { v, actionId, _ ->

                // Show the keyboard
                var inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(holder.personNameEditText, InputMethodManager.SHOW_IMPLICIT)

                // If the text is confirmed
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    // Get the enteredText and capitalise it
                    val enteredText = holder.personNameEditText.text.toString().lowercase().replaceFirstChar { it.uppercaseChar() }

                    // Hide the keyboard
                    inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                    // Check if the entered person does not already exist in the person list
                    if (!personList.contains(Person(enteredText))) {

                        // if so, set the current person and adjust the UI and person list
                        currentPerson.name = enteredText
                        holder.personNameEditText.setText(currentPerson.name)
                        PersonListSingleton.personList[position].name = currentPerson.name

                        // Set shared preferences and save person List
                        val sharedPreferences = LocalSharedPreferences(context)
                        sharedPreferences.savePersonListPref(currentRoom)

                        // Clear the gifting list and save
                        GiftingListSingleton.giftingList.clear()
                        sharedPreferences.removeGiftingListPref(currentRoom)

                        // Adjust edit text variables
                        holder.personNameEditText.isFocusable = false
                        holder.personNameEditText.isClickable = false

                        true
                    } else {

                        // If not, inform the user that this person already exists
                        holder.personNameEditText.text.clear()
                        Toast.makeText(
                            context,
                            "This person already exists",
                            Toast.LENGTH_SHORT
                        ).show()

                        false
                    }

                } else {

                    false
                }
            }
        }

        // Set person name edit text on click listener for showing details window
        holder.personNameEditText.setOnClickListener {

            // If the person is valid, show the details window
            if (currentPerson.name.isNotEmpty()) {
                personWindow.showPersonWindow(currentPerson.name)
            }
        }

        // Set details button on click listener for showing details window
        holder.personDetailsButton.setOnClickListener {

            // If the person is valid, show the details window
            if (currentPerson.name.isNotEmpty()) {
                personWindow.showPersonWindow(currentPerson.name)
            }
        }

        // Set remove button on click listener for removing this person
        holder.personRemoveButton.setOnClickListener {

            removePerson(position)
            // Hide the keyboard
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(holder.personNameEditText.windowToken, 0)
        }

    }

    /**
     * This function returns the total number of items in the RecyclerView
     *
     * @return the number of items
     */
    override fun getItemCount(): Int {
        return personList.size
    }

    /**
     * This function removes a person from the list
     *
     * @param position the position of the person that is being removed
     */
    private fun removePerson(position: Int) {

        // remove the person from the list
        personList.removeAt(position)

        // Get sharedPreferences
        val sharedPreferences = LocalSharedPreferences(context)
        sharedPreferences.savePersonListPref(currentRoom)

        // Clear the gifting list and save
        GiftingListSingleton.giftingList.clear()
        sharedPreferences.removeGiftingListPref(currentRoom)

        // Notify the recycler
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, personList.size - position)

    }

}