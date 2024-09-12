package com.talhaatif.jobportalclient.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.talhaatif.jobportalclient.ChatScreen
import com.talhaatif.jobportalclient.R
import com.talhaatif.jobportalclient.databinding.RvProfileBinding
import com.talhaatif.jobportalclient.model.Job
import com.talhaatif.jobportalclient.model.User

class UserAdapter(private var users : List<User>,  private val activity: Activity) : RecyclerView.Adapter<UserAdapter.UserViewHolder> (){


    class UserViewHolder(val binding: RvProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = RvProfileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return  users.size
    }

    fun updateUsers(newusers: MutableList<User>) {
        this.users = newusers
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.userName.text = user.username
        holder.binding.userRole.text = user.role
        Glide.with(holder.binding.userProfileImage.context)
            .load(user.profilePic)
            .placeholder(R.drawable.cartoon_happy_eyes)
            .into(holder.binding.userProfileImage)

        // going to chat screen whom he wanna have a chat
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatScreen::class.java).apply {
                putExtra("uid", user.uid)
                putExtra("profilePic", user.profilePic)
                putExtra("userName",user.username)

            }
            activity.startActivity(intent)
        }
    }
}