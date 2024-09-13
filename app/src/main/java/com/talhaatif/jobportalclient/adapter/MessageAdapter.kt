package com.talhaatif.jobportalclient.adapter

import android.media.MediaPlayer
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.talhaatif.jobportalclient.R
import com.talhaatif.jobportalclient.databinding.ItemMessageBinding
import com.talhaatif.jobportalclient.model.Message
import java.io.IOException

class MessageAdapter(private var messages: List<Message>, private val currentUID: String) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var handler: Handler = Handler()

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Text message handling
        holder.binding.textMessage.text = message.messageText
        if (message.senderId == currentUID) {
            (holder.binding.textMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.END
            (holder.binding.voiceContainer.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.END
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_send)
            holder.binding.voiceContainer.setBackgroundResource(R.drawable.bg_send)
        } else {
            (holder.binding.textMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            (holder.binding.voiceContainer.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_receive)
            holder.binding.voiceContainer.setBackgroundResource(R.drawable.bg_receive)
        }

        // Check if it's a text message or a voice message
        if (message.messageType == "text") {
            // Show the text message and hide the voice message UI
            holder.binding.textMessage.text = message.messageText
            holder.binding.textMessage.visibility = View.VISIBLE
            holder.binding.voiceContainer.visibility = View.GONE

        } else if (message.messageType == "voice") {
            // Hide the text message and show the voice message UI
            holder.binding.textMessage.visibility = View.GONE
            holder.binding.voiceContainer.visibility = View.VISIBLE

            holder.binding.btnPlayVoiceNote.setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    holder.binding.btnPlayVoiceNote.setImageResource(R.drawable.ic_play)
                } else {
                    playVoiceMessage(holder, message.messageText) // messageText contains the audio URL
                    holder.binding.btnPlayVoiceNote.setImageResource(R.drawable.ic_pause)
                }
            }

            // Set up SeekBar for voice note
            holder.binding.seekBarVoiceNote.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun playVoiceMessage(holder: MessageViewHolder, audioUrl: String) {
        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(audioUrl)
            mediaPlayer?.prepare()
            mediaPlayer?.start()

            // Set SeekBar max value to the audio duration
            holder.binding.seekBarVoiceNote.max = mediaPlayer!!.duration

            // Update the duration TextView
            val totalDuration = mediaPlayer!!.duration / 1000 // Convert to seconds
            holder.binding.txtVoiceNoteDuration.text = formatDuration(totalDuration)

            // Update SeekBar as audio plays
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (mediaPlayer != null) {
                        holder.binding.seekBarVoiceNote.progress = mediaPlayer!!.currentPosition
                        handler.postDelayed(this, 1000)
                    }
                }
            }, 1000)

            // On completion of audio playback
            mediaPlayer?.setOnCompletionListener {
                holder.binding.btnPlayVoiceNote.setImageResource(R.drawable.ic_play)
                holder.binding.seekBarVoiceNote.progress = 0
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}
