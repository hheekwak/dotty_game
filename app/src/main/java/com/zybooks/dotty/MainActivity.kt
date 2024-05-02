package com.zybooks.dotty

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.graphics.Bitmap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.zybooks.dotty.DotsView.DotsGridListener
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val dotsGame = DotsGame.getInstance()
    private lateinit var photoImageView: ImageView
    private lateinit var dotsView: DotsView
    private lateinit var movesRemainingTextView: TextView
    private lateinit var scoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.piano_inst_yooheeyull_dacapo)
        mediaPlayer?.start()

        setContentView(R.layout.activity_main)

        // Initialize photoImageView
        photoImageView = findViewById(R.id.photoImageView)
        movesRemainingTextView = findViewById(R.id.moves_remaining_text_view)
        scoreTextView = findViewById(R.id.score_text_view)
        dotsView = findViewById(R.id.dots_view)

        // Set up takePicturePreview
        val takePicturePreview: ActivityResultLauncher<Void?> = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { thumbnail: Bitmap? ->
            thumbnail?.let {
                photoImageView.setImageBitmap(thumbnail)
            }
        }

        // Set up button click listener
        val takePhotoButton = findViewById<Button>(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener { takePhotoClick(takePicturePreview) }

        findViewById<Button>(R.id.new_game_button).setOnClickListener { newGameClick() }

        dotsView.setGridListener(gridListener)

        startNewGame()
    }

    private val gridListener = object : DotsGridListener {
        override fun onDotSelected(dot: Dot, status: DotSelectionStatus) {
            // Ignore selections when game is over
            if (dotsGame.isGameOver) return

            // Add/remove dot to/from selected dots
            val addStatus = dotsGame.processDot(dot)

            // If done selecting dots then replace selected dots and display new moves and score
            if (status === DotSelectionStatus.Last) {
                if (dotsGame.selectedDots.size > 1) {
                    dotsView.animateDots()

                    // These methods must be called AFTER the animation completes
                    //dotsGame.finishMove()
                    //updateMovesAndScore()
                } else {
                    dotsGame.clearSelectedDots()
                }
            }

            // Display changes to the game
            dotsView.invalidate()
        }
        override fun onAnimationFinished() {
            dotsGame.finishMove()
            dotsView.invalidate()
            updateMovesAndScore()
        }
    }

    private fun newGameClick() {
        // Animate down off screen
        val screenHeight = this.window.decorView.height.toFloat()
        val moveBoardOff = ObjectAnimator.ofFloat(
            dotsView, "translationY", screenHeight)
        moveBoardOff.duration = 700
        moveBoardOff.start()

        moveBoardOff.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startNewGame()

                // Animate from above the screen down to default location
                val moveBoardOn = ObjectAnimator.ofFloat(
                    dotsView, "translationY", -screenHeight, 0f)
                moveBoardOn.duration = 700
                moveBoardOn.start()
            }
        })
    }

    private fun startNewGame() {
        dotsGame.newGame()
        dotsView.invalidate()
        updateMovesAndScore()
    }

    private fun updateMovesAndScore() {
        movesRemainingTextView.text = String.format(Locale.getDefault(), "%d", dotsGame.movesLeft)
        scoreTextView.text = String.format(Locale.getDefault(), "%d", dotsGame.score)
    }

    private fun takePhotoClick(takePicturePreview: ActivityResultLauncher<Void?>) {
        // Launch the takePicturePreview activity
        takePicturePreview.launch(null)
    }
}