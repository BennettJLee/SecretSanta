package com.example.secretsanta

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

    private lateinit var personWindow : PersonWindow

    inner class PersonViewHolder(binding: PersoncardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val personNameEditText: EditText = binding.personNameEditText
        val removePersonButton: RelativeLayout = binding.removePersonButton
        val personDetailsButton: RelativeLayout = binding.personDetailsButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val binding = PersoncardItemBinding.inflate(inflater, parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {

        val currentPerson = personList[position]

        Log.e("test", currentPerson.name)

        personWindow = PersonWindow(context)

        holder.personNameEditText.setText(currentPerson.name)

        holder.personNameEditText.isFocusable = currentPerson.name.isEmpty()
        holder.personNameEditText.isClickable = currentPerson.name.isEmpty()

        if (currentPerson.name.isEmpty()) {
            holder.personNameEditText.setOnEditorActionListener { v, actionId, _ ->

                var inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(
                    holder.personNameEditText,
                    InputMethodManager.SHOW_IMPLICIT
                )

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredText = holder.personNameEditText.text.toString()
                        .replaceFirstChar { it.uppercaseChar() }

                    inputMethodManager =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)


                    if (!personList.contains(Person(enteredText))) {

                        holder.personNameEditText.setText(enteredText)
                        currentPerson.name = enteredText

                        PersonListSingleton.personList[position].name = enteredText
                        val sharedPreferences = LocalSharedPreferences(context)
                        sharedPreferences.savePersonListPref(currentRoom)

                        GiftingListSingleton.giftingList.clear()
                        sharedPreferences.removeGiftingListPref(currentRoom)

                        holder.personNameEditText.isFocusable = false
                        holder.personNameEditText.isClickable = false

                        true

                    } else {

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

        holder.personNameEditText.setOnClickListener {

            if (currentPerson.name.isNotEmpty()) {
                personWindow.showPersonWindow(currentPerson.name)
            }
        }

        holder.personDetailsButton.setOnClickListener {

            if (currentPerson.name.isNotEmpty()) {
                personWindow.showPersonWindow(currentPerson.name)
            }
        }

        holder.removePersonButton.setOnClickListener {

            removePerson(position)
        }

    }

    override fun getItemCount(): Int {
        return personList.size
    }

    private fun removePerson(position: Int) {

        personList.removeAt(position)

        val sharedPreferences = LocalSharedPreferences(context)
        sharedPreferences.savePersonListPref(currentRoom)

        GiftingListSingleton.giftingList.clear()
        sharedPreferences.removeGiftingListPref(currentRoom)

        notifyItemRemoved(position)

        notifyItemRangeChanged(position, personList.size - position)

    }

}