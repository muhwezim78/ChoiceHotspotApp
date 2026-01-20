package com.muhwezi.choicehotspot.ui.vouchers;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.voucher.Voucher;

import java.util.ArrayList;
import java.util.List;

public class VoucherResultDialog extends DialogFragment {

    private List<Voucher> vouchers = new ArrayList<>();

    public static VoucherResultDialog newInstance(List<Voucher> vouchers) {
        VoucherResultDialog fragment = new VoucherResultDialog();
        fragment.vouchers = vouchers;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_voucher_result, null);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_results);
        Button closeButton = view.findViewById(R.id.btn_close);

        VoucherAdapter adapter = new VoucherAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        adapter.setVouchers(vouchers);

        closeButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
