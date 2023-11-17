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

class PersonAdapter(private val context: Context, private val currentRoom: String, private val personList: MutableList<Person>) :
RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(binding: PersoncardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val personNameEditText: EditText = binding.personNameEditText
        val removePersonButton: RelativeLayout = binding.removePersonButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val binding = PersoncardItemBinding.inflate(inflater, parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {

        val currentPerson = personList[position]

        if (currentPerson.name != "") {
            holder.personNameEditText.setText(currentPerson.name)
            holder.personNameEditText.isFocusable = false
        } else {

            holder.personNameEditText.isFocusable = true
            holder.personNameEditText.requestFocus()

            holder.personNameEditText.setOnEditorActionListener { v, actionId, _ ->

                var inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(holder.personNameEditText, 0)

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredText = holder.personNameEditText.text.toString()
                        .replaceFirstChar { it.uppercaseChar() }

                    if (!personList.contains(Person(enteredText))) {

                        holder.personNameEditText.setText(enteredText)
                        currentPerson.name = enteredText

                        PersonListSingleton.personList[position].name = enteredText
                        val sharedPreferences = LocalSharedPreferences(context)
                        sharedPreferences.savePersonListPref(currentRoom)

                        holder.personNameEditText.isFocusable = false

                    } else {

                        holder.personNameEditText.text.clear()
                        Toast.makeText(context, "This person already exists", Toast.LENGTH_SHORT).show()
                    }

                    inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                    true
                } else {
                    false
                }
            }

        }

        holder.removePersonButton.setOnClickListener {

            personList.removeAt(position)

            PersonListSingleton.personList.remove(currentPerson)
            val sharedPreferences = LocalSharedPreferences(context)
            sharedPreferences.savePersonListPref(currentRoom)

            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    private fun removePerson(position: Int) {
        personList.removeAt(position)
        notifyItemRemoved(position)

        PersonListSingleton.personList.removeAt(position)

        val sharedPreferences = LocalSharedPreferences(context)
        sharedPreferences.savePersonListPref(currentRoom)

        // Notify the adapter that the remaining items after the removed position might have changed
        notifyItemRangeChanged(position, personList.size - position)

    }

}