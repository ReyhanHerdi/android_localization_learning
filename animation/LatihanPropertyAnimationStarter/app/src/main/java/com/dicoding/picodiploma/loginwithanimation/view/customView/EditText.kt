package com.dicoding.picodiploma.loginwithanimation.view.customView

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.loginwithanimation.R

class EditText : AppCompatEditText, View.OnTouchListener {

    private lateinit var lockImage: Drawable

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttibute: Int) : super(context, attributeSet, defStyleAttibute){
        init()
    }

    private fun init() {
        lockImage = ContextCompat.getDrawable(context, R.drawable.baseline_lock_24) as Drawable

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
                // Do nothinf
            }

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                if (text.toString().length < 8) {
                    setError("Password kurang dari 8 karakter", null)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(text: Editable?) {
                // Do nothinf
            }

        })
        showLockIcon()
    }

    private fun showLockIcon() {
        setButtonDrawables(startOfTheText = lockImage)
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        return false
    }
}