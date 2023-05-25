package com.zcode.crashcar.ui.home.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.zcode.crashcar.MainApplication.Companion.prefsSetting
import com.zcode.crashcar.R
import com.zcode.crashcar.api.response.Usuario
import com.zcode.crashcar.databinding.FragmentProfileBinding
import com.zcode.crashcar.ui.MainActivity
import com.zcode.crashcar.ui.home.profile.activities.ChangePasswrodActivity
import com.zcode.crashcar.ui.home.profile.activities.EditUserActivity
import com.zcode.crashcar.utils.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var user: Usuario
    private lateinit var mGoogleApiClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initGoogleApi()
        return binding.root
    }

    private fun initGoogleApi() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        mGoogleApiClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initComponent()
    }

    private fun initComponent() {
        loadScreenData()
        binding.btnLogOutProfileSetting.setOnClickListener { logOut() }
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(
                requireContext(),
                EditUserActivity::class.java
            )

            intent.putExtra("code", 1)

            startActivity(
                intent
            )
        }
        binding.btnChangePassword.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    ChangePasswrodActivity::class.java
                )
            )
        }

        if (prefsSetting.getTypeLogin() == 2) {
            binding.spaceChangePassword.visibility = View.GONE
            binding.btnChangePassword.visibility = View.GONE
        }
    }

    private fun loadScreenData() {
        user = prefsSetting.getUser()!!
        binding.lblNombreProfile.text = String.format("%s %s", user.nombre, user.apellidos)
        loadStatsAccount()
    }

    private fun loadStatsAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListSeguros(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.lblSegurosRegistradosProfile.text = String.format(
                            getString(R.string.registerSeguros), response.body()?.size
                        )
                    }
                } else {
                    Log.e("ErrorSegurosResponse", response.message().toString())
                }
            } catch (e: Exception) {
                Log.e("ConnectionError", e.message.toString())
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListVehiculos(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.lblCochesRegistradosProfile.text = String.format(
                            getString(R.string.registerCar),
                            response.body()?.size.toString()
                        )
                    }
                } else {
                    Log.e("ErrorSegurosResponse", response.message().toString())
                }
            } catch (e: Exception) {
                Log.e("ConnectionError", e.message.toString())
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitObject.getCallRetrofit().getListPartes(prefsSetting.getIdUser())
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        binding.lblPartesRegistradosProfile.text = String.format(
                            getString(R.string.registerPartes),
                            response.body()?.size.toString()
                        )
                    }
                } else {
                    Log.e("ErrorSegurosResponse", response.message().toString())
                }
            } catch (e: Exception) {
                Log.e("ConnectionError", e.message.toString())
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun logOut() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verify_close_session, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val btnOk: FrameLayout = dialogView.findViewById(R.id.btn_ok_close_session)
        val btnCancel: FrameLayout = dialogView.findViewById(R.id.btn_cancel_close_session)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)

        btnOk.setOnClickListener {
            alertDialog.dismiss()
            backLogin()
        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    override fun onResume() {
        super.onResume()
        loadScreenData()
    }

    private fun backLogin() {
        if (prefsSetting.getTypeLogin() == 2) {
            mGoogleApiClient.signOut()
        }
        prefsSetting.resetPrefs()
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }
}