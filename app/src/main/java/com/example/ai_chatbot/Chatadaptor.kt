package com.example.ai_chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val chatList: ArrayList<ChatModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].isUser) 1 else 2
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        if (viewType == 1) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_user, parent, false)

            return UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_ai, parent, false)

            return AiViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val chat = chatList[position]

        if (holder is UserViewHolder) {
            holder.message.text = chat.message
        }

        if (holder is AiViewHolder) {
            holder.message.text = chat.message
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.userMessage)
    }

    class AiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.aiMessage)
    }
}
