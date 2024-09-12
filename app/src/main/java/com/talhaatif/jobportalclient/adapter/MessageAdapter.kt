package com.talhaatif.jobportalclient.adapter

import android.text.Layout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.talhaatif.jobportalclient.R
import com.talhaatif.jobportalclient.databinding.ItemMessageBinding
import com.talhaatif.jobportalclient.model.Message

class MessageAdapter(private var messages: List<Message>, private val currentUID:String ) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding : ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  MessageViewHolder(binding) // return binding here

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.binding.textMessage.text = message.messageText
        if (message.senderId == currentUID){
            ( holder.binding.textMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.END
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_send)

        }
        else{
            ( holder.binding.textMessage.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_receive)
        }

    }
}