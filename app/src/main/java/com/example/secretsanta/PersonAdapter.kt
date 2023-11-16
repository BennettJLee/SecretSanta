package com.example.secretsanta

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView
import com.example.secretsanta.databinding.PersoncardItemBinding

class PersonAdapter(private val context: Context, private val recyclerView: RecyclerView, private val currentRoom: String, private val personList: MutableList<Person>) :
RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(private val binding: PersoncardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val personNameEditText: EditText = binding.personNameEditText
        val removePersonButton: RelativeLayout = binding.removePersonButton
        val personItemLayout: LinearLayout = binding.personItemLayout
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

            holder.personNameEditText.setOnEditorActionListener { v, actionId, event ->


                var inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(
                    holder.personNameEditText,
                    InputMethodManager.SHOW_IMPLICIT
                )

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredText = holder.personNameEditText.text.toString()
                        .replaceFirstChar { it.uppercaseChar() }

                    holder.personNameEditText.setText(enteredText)
                    currentPerson.name = enteredText

                    PersonListSingleton.personList[position].name = enteredText
                    val sharedPreferences = LocalSharedPreferences(context)
                    sharedPreferences.savePersonListPref(currentRoom)

                    holder.personNameEditText.isFocusable = false

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

            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return personList.size
    }


    /*private fun editPerson(){

        personNameEditText.isFocusable = true
        personNameEditText.hint = "Enter Name..."

        personNameEditText.requestFocus()

        var inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(personNameEditText, 0)

        personNameEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // get the enteredText and capitalise it
                val enteredText = personNameEditText.text.toString().replaceFirstChar { it.uppercaseChar() }

                PersonListSingleton.personList.add(Person(enteredText))

                inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)

                personNameEditText.isFocusable = false
                true
            } else {
                false
            }
        }
    }*/

}