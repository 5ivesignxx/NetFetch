package com.example.netfetchdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(private var todos: List<Todo>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView       = itemView.findViewById(R.id.tvTodoId)
        val tvTitle: TextView    = itemView.findViewById(R.id.tvTodoTitle)
        val tvStatus: TextView   = itemView.findViewById(R.id.tvTodoStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.tvId.text    = "#${todo.id}"
        holder.tvTitle.text = todo.title
        holder.tvStatus.text = if (todo.completed) "✓ Done" else "○ Pending"
        holder.tvStatus.setTextColor(
            if (todo.completed)
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            else
                holder.itemView.context.getColor(android.R.color.holo_blue_dark)
        )
    }

    override fun getItemCount() = todos.size

    fun updateList(newTodos: List<Todo>) {
        todos = newTodos
        notifyDataSetChanged()
    }
}
