package com.zcode.crashcar.utils.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import com.zcode.crashcar.R

/*
 *    Created by Nono on 25/05/2023.
 */
object DialogAlert {
    fun showDialogAlert(context: Context, msg: String, idLottie: Int) {
      val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null, false)
      val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)

      val btnOk: FrameLayout = dialogView.findViewById(R.id.btnOk)
      val textMsg: TextView = dialogView.findViewById(R.id.textMsgAlertDialog)
      val imgLottie: LottieAnimationView = dialogView.findViewById(R.id.imgAnimatedAlertDialog)

      textMsg.text = msg
      imgLottie.setAnimation(idLottie)
      imgLottie.loop(true)
      imgLottie.playAnimation()

      val alertDialog = dialogBuilder.create()
      alertDialog.window?.setBackgroundDrawableResource(R.drawable.shape_dialog)
      btnOk.setOnClickListener { alertDialog.dismiss() }

      alertDialog.show()
    }
}