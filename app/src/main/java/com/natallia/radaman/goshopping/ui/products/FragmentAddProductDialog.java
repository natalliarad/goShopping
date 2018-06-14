package com.natallia.radaman.goshopping.ui.products;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.natallia.radaman.goshopping.R;

public class FragmentAddProductDialog extends DialogFragment {
    EditText editTextProductName;

    /**
     * Public static constructor that creates fragment and
     * passes a bundle with data into it when adapter is created
     */
    public static FragmentAddProductDialog newInstance() {
        FragmentAddProductDialog addProductDialogFragment = new FragmentAddProductDialog();
        Bundle bundle = new Bundle();
        addProductDialogFragment.setArguments(bundle);
        return addProductDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /* Use the Builder class for convenient dialog construction */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        /* Get the layout inflater */
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_add_product_dialog, null);
        editTextProductName = rootView.findViewById(R.id.edit_text_product_name);

        /**
         * Call addProduct() when user taps "Done" keyboard action
         */
        editTextProductName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent
                        .ACTION_DOWN) {
                    addProduct();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout */
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.button_positive_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addProduct();
                    }
                });

        return builder.create();
    }

    /**
     * Add new product
     */
    public void addProduct() {
    }
}
