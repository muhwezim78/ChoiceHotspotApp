package com.muhwezi.choicehotspot.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.auth.LoginResponse;
import com.muhwezi.choicehotspot.repository.ApiRepository;
import com.muhwezi.choicehotspot.utils.ApiUtils;

/**
 * Fragment for handling user login.
 */
public class LoginFragment extends Fragment {

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button forgotPasswordButton;
    private TextView errorText;
    private CircularProgressIndicator loadingIndicator;
    private LoginListener listener;

    public interface LoginListener {
        void onLoginSuccess();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    public void setLoginListener(LoginListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        emailLayout = view.findViewById(R.id.email_layout);
        emailInput = view.findViewById(R.id.email_input);
        passwordLayout = view.findViewById(R.id.password_layout);
        passwordInput = view.findViewById(R.id.password_input);
        loginButton = view.findViewById(R.id.login_button);
        forgotPasswordButton = view.findViewById(R.id.forgot_password_button);
        errorText = view.findViewById(R.id.error_text);
        loadingIndicator = view.findViewById(R.id.loading_indicator);

        // Set up listeners
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPasswordButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Password reset feature coming soon", Toast.LENGTH_SHORT).show();
            // TODO: Implement password reset dialog/fragment
        });
    }

    private void attemptLogin() {
        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);
        errorText.setVisibility(View.GONE);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Validation
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            focusView = passwordInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            focusView = emailInput;
            cancel = true;
        } else if (!ApiUtils.isValidEmail(email)) {
            emailLayout.setError("Invalid email address");
            focusView = emailInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        showLoading(true);

        // For now, assume simple login. In a real app we might get device info.
        // String deviceInfo = "Android App " + android.os.Build.MODEL;

        // Use adminLogin for the admin app
        ApiRepository.getInstance().adminLogin(email, password, new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                if (isAdded()) {
                    showLoading(false);
                    if (data.isSuccess()) {
                        Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onLoginSuccess();
                        }
                    } else {
                        showError(data.getMessage());
                    }
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    showLoading(false);
                    showError(message);
                }
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loginButton.setVisibility(View.INVISIBLE);
            loadingIndicator.setVisibility(View.VISIBLE);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
