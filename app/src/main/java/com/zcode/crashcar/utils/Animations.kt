package com.zcode.crashcar.utils

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.zcode.crashcar.R

/*
 *    Created by Nono on 25/05/2023.
 */
object Animations {
    fun showAnimation(context: Context): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.anim_show_block)
    }

    fun hideAnimation(context: Context): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.anim_hide_block)
    }

    fun fadeInAnimation(context: Context): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.fade_in)
    }

    fun fadeOutAnimation(context: Context): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.fade_out)
    }
}