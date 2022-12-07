package com.example.gymline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.example.gymline.databinding.CourseItemBinding
import com.google.firebase.database.*

class CourseAdapter: RecyclerView.Adapter<CourseAdapter.CourseHolder>() {
    val courseList = ArrayList<FullCourse>()

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(fullCourse: FullCourse, item: View)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    class CourseHolder(item: View, listener: OnItemClickListener): RecyclerView.ViewHolder(item) {
        val binding = CourseItemBinding.bind(item)
        private lateinit var fullCourse1: FullCourse

        fun bind(fullCourse: FullCourse) = with(binding){
            courseTitle.text = fullCourse.title
            courseAuthor.text = fullCourse.author
            courseDesc.text = fullCourse.desc
            courseImg.setImageBitmap(fullCourse.img)
            id.text = fullCourse.id.toString()
            fullCourse1 = fullCourse
        }

        init{
            item.setOnClickListener{
               listener.onItemClick(fullCourse1, item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.course_item, parent, false)
        return CourseHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: CourseHolder, position: Int) {
        holder.bind(courseList[position])
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    fun addCourse(fullCourse: FullCourse) {
        courseList.add(fullCourse)
        notifyDataSetChanged()
    }
    fun clear() {
        val size = courseList.size
        courseList.clear()
        notifyItemRangeRemoved(0, size)
    }

}

