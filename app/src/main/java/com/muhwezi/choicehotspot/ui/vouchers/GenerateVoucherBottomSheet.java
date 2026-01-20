package com.muhwezi.choicehotspot.ui.vouchers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.profile.Profile;
import com.muhwezi.choicehotspot.models.voucher.Voucher;
import com.muhwezi.choicehotspot.models.voucher.VoucherGenerateRequest;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.util.ArrayList;
import java.util.List;

public class GenerateVoucherBottomSheet extends BottomSheetDialogFragment {

    private TextInputLayout profileDropdown;
    private TextInputLayout quantityInputLayout;
    private TextInputLayout customerNameInputLayout;
    private TextInputLayout customerContactInputLayout;
    private RadioGroup passwordTypeGroup;
    private Button generateButton;
    private Button cancelButton;

    private List<Profile> profiles = new ArrayList<>();
    private String selectedProfileArg = "";

    public GenerateVoucherBottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_generate_voucher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileDropdown = view.findViewById(R.id.profile_dropdown);
        quantityInputLayout = view.findViewById(R.id.quantity_input);
        customerNameInputLayout = view.findViewById(R.id.customer_name_input);
        customerContactInputLayout = view.findViewById(R.id.customer_contact_input);
        passwordTypeGroup = view.findViewById(R.id.password_type_group);
        generateButton = view.findViewById(R.id.generate_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        loadProfiles();

        generateButton.setOnClickListener(v -> generate());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void loadProfiles() {
        ApiRepository.getInstance().getEnhancedProfiles(true, new ApiCallback<List<Profile>>() {
            @Override
            public void onSuccess(List<Profile> data) {
                if (data != null) {
                    profiles = data;
                    setupDropdown();
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load profiles", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupDropdown() {
        if (getContext() == null)
            return;

        List<String> profileNames = new ArrayList<>();
        for (Profile p : profiles) {
            profileNames.add(p.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
                profileNames);
        ((AutoCompleteTextView) profileDropdown.getEditText()).setAdapter(adapter);

        ((AutoCompleteTextView) profileDropdown.getEditText()).setOnItemClickListener((parent, v, position, id) -> {
            selectedProfileArg = profileNames.get(position);
        });

        if (!profileNames.isEmpty()) {
            ((AutoCompleteTextView) profileDropdown.getEditText()).setText(profileNames.get(0), false);
            selectedProfileArg = profileNames.get(0);
        }
    }

    private void generate() {
        String quantityStr = quantityInputLayout.getEditText().getText().toString();
        int quantity = 1;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            quantityInputLayout.setError("Invalid number");
            return;
        }

        if (selectedProfileArg.isEmpty()) {
            profileDropdown.setError("Select a profile");
            return;
        }

        String customerName = customerNameInputLayout.getEditText().getText().toString().trim();
        String customerContact = customerContactInputLayout.getEditText().getText().toString().trim();

        String passwordType = "blank";
        int checkedId = passwordTypeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_same) {
            passwordType = "same";
        } else if (checkedId == R.id.radio_custom) {
            passwordType = "custom";
        }

        VoucherGenerateRequest request = new VoucherGenerateRequest();
        request.setProfileName(selectedProfileArg);
        request.setQuantity(quantity);
        request.setCustomerName(customerName);
        request.setCustomerContact(customerContact);
        request.setPasswordType(passwordType);

        generateButton.setEnabled(false);
        generateButton.setText("Generating...");

        ApiRepository.getInstance().generateVouchers(request, new ApiCallback<List<Voucher>>() {
            @Override
            public void onSuccess(List<Voucher> data) {
                if (getContext() != null) {
                    dismiss();
                    VoucherResultBottomSheet resultBottomSheet = VoucherResultBottomSheet.newInstance(data);
                    resultBottomSheet.show(getParentFragmentManager(), "VoucherResultBottomSheet");
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                    generateButton.setEnabled(true);
                    generateButton.setText("Generate");
                }
            }
        });
    }
}
