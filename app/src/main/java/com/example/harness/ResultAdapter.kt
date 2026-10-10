package com.example.harness

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.harness.databinding.RowResultBinding

class ResultAdapter : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
    private val rows = mutableListOf<Pair<String, String>>()

    fun submitRows(newRows: List<Pair<String, String>>) {
        rows.clear()
        rows.addAll(newRows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = RowResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val row = rows[position]
        holder.bind(row.first, row.second)
    }

    override fun getItemCount(): Int = rows.size

    class ResultViewHolder(private val binding: RowResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, subtitle: String) {
            binding.txtTitle.text = title
            binding.txtSubtitle.text = subtitle
        }
    }
}
