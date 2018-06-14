package com.natallia.radaman.goshopping.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

public class AppFormValidatonUtils {
    public static boolean isBlank(EditText editText) {
        return TextUtils.isEmpty(editText.getText());
    }

    public static boolean isEmailValid(EditText editText) {
        return Patterns.EMAIL_ADDRESS.matcher(editText.getText()).matches();
    }

    public static void clearErrors(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setError(null);
        }
    }

    public static boolean isPasswordValid(String password)
    {
        return password.length() > 4 && password.length() < 16;
    }

    public static void clearForm(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setText("");
        }
    }
}
