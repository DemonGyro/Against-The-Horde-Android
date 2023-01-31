package com.github.againstthehorde.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.*
import android.util.AttributeSet
import android.util.Base64
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.github.againstthehorde.R

class DeckPreviewLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    var contentRotation : Float = 5f
    var borderWidth : Int = 3
    var borderColor : Int = Color.BLACK

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        if (child == null) {
            return
        }

        val prefs = context.getSharedPreferences("Decks", Context.MODE_PRIVATE)
        var imageDrawable : Drawable
        val imageBytes = prefs.getString("Deck_" + (childCount - 1) + "_Image", null)
        if (imageBytes != null) {
            val bytes = Base64.decode(imageBytes.toByteArray(), Base64.DEFAULT)
            imageDrawable = BitmapDrawable(context.resources, BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        } else {
            imageDrawable = ContextCompat.getDrawable(context, R.drawable.ic_zombie)!!
        }

        // Sets up the rotation and image anti-rotation with clipping
        child.rotation = contentRotation
        child.clipToOutline = true
        val rotateDrawable = RotateDrawable()
        rotateDrawable.drawable = imageDrawable
        rotateDrawable.fromDegrees = -contentRotation
        rotateDrawable.toDegrees = -contentRotation

        // Sets the border on the edges of the image
        var colorDrawable = ColorDrawable(borderColor)
        val layers : Array<Drawable> = arrayOf(colorDrawable, rotateDrawable)
        val layerDrawable = LayerDrawable(layers)
        layerDrawable.setLayerInset(0, 0, 0, 0, 0)
        layerDrawable.setLayerInset(1, 0, borderWidth, 0, borderWidth)

        child.background = layerDrawable
        child.scaleX = 1.2f
        child.scaleY = 1.2f
    }
}