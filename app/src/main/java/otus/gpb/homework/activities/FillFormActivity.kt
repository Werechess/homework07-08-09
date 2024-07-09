package otus.gpb.homework.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class FillFormActivity : Activity() {

    private lateinit var editName: EditText
    private lateinit var editSurname: EditText
    private lateinit var editAge: EditText
    private lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_form)

        editName = findViewById(R.id.editName)
        editSurname = findViewById(R.id.editSurname)
        editAge = findViewById(R.id.editAge)
        applyButton = findViewById(R.id.button_apply)

        applyButton.setOnClickListener {
            intent = Intent()
                .putExtra("name", editName.text.toString())
                .putExtra("surname", editSurname.text.toString())
                .putExtra("age", editAge.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}