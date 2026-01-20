package com.muhwezi.choicehotspot.ui.vouchers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.models.voucher.Voucher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VoucherResultBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_VOUCHERS = "vouchers";
    private List<Voucher> vouchers;

    public static VoucherResultBottomSheet newInstance(List<Voucher> vouchers) {
        VoucherResultBottomSheet fragment = new VoucherResultBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VOUCHERS, (Serializable) vouchers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vouchers = (List<Voucher>) getArguments().getSerializable(ARG_VOUCHERS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_voucher_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        VoucherAdapter adapter = new VoucherAdapter(getContext(), vouchers);
        recyclerView.setAdapter(adapter);

        Button btnClose = view.findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
    }
}
