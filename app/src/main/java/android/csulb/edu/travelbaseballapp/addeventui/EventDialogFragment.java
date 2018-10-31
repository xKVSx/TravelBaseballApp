package android.csulb.edu.travelbaseballapp.addeventui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.csulb.edu.travelbaseballapp.R;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EventDialogFragment extends DialogFragment {
    public static String LOCATION_NAME = "location_name";
    public static String ADDRESS = "address";
    public static String DESCRIPTION = "description";
    private EditText mLocationNameText;
    private EditText mAddressText;
    private EditText mDescriptionText;
    private EditEventInfoDialogListener mDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View rootView = layoutInflater.inflate(R.layout.event_dialog, null);
        mLocationNameText = rootView.findViewById(R.id.location_name_edit);
        mAddressText = rootView.findViewById(R.id.address_edit);
        mDescriptionText = rootView.findViewById(R.id.description_edit);
        builder.setTitle(getString(R.string.add_event_info));
        builder.setView(rootView)
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /*add the entered information to the created event and pass it to the
                        parent Activity*/
                        String location = mLocationNameText.getText().toString();
                        String address = mAddressText.getText().toString();
                        String description = mDescriptionText.getText().toString();
                        Bundle eventBundle = new Bundle();
                        eventBundle.putString(LOCATION_NAME, location);
                        eventBundle.putString(ADDRESS, address);
                        eventBundle.putString(DESCRIPTION, description);
                        mDialogListener.onFinishEditDialog(eventBundle);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing and exit the dialog.
                    }
                });
        return builder.create();
    }

    public interface EditEventInfoDialogListener{
        void onFinishEditDialog(Bundle eventBundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Verify that the host activity implements the callback interface
        try {
            //Instantiate the EditEventInfoDialogListener so we can send events to the host
            mDialogListener = (EditEventInfoDialogListener) context;
        } catch (ClassCastException e) {
            //The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + getString(R.string.event_dialog_interface_error));
        }
    }
}
