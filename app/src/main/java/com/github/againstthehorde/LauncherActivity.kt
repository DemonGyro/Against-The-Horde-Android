package com.github.againstthehorde

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.againstthehorde.databinding.ActivityLauncherBinding
import com.github.againstthehorde.model.DeckManager
import java.util.*
import kotlin.random.Random

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Random(Date().time)

        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backgroundImage.setImageResource(this.getRandomLaunchImage())
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()

        DeckManager.createStarterDecks(this)
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun getRandomLaunchImage(): Int {
        val options = listOf(
            R.drawable.deck_picker_img__dinosaur,
            R.drawable.deck_picker_img__eldrazi,
            R.drawable.deck_picker_img__human,
            R.drawable.deck_picker_img__nature,
            R.drawable.deck_picker_img__phyrexian,
            R.drawable.deck_picker_img__zombie)

        return options.shuffled().first()
    }
}