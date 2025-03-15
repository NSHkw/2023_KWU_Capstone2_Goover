package com.example.gooverapplication

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class ViewPager2Adapter(
    private val context: Context,
    private val imageList: List<Uri>
): RecyclerView.Adapter<ViewPager2Adapter.PagerViewHolder>() {

    inner class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val item: ImageView = itemView.findViewById(R.id.imageView1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.item_detail_wrong,
            parent,
            false
        )
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.item.setImageURI(imageList[position])

        holder.item.setOnClickListener {
            showImageDialog(context, imageList[position])
        }
    }

    private fun showImageDialog(context: Context, imageUri: Uri) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image, null)
        val dialogImageView: ImageView = dialogView.findViewById(R.id.dialogImageView)
        dialogImageView.setImageURI(imageUri)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun getItemCount() = imageList.size
}