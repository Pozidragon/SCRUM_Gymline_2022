package com.example.gymline

import android.graphics.Bitmap

open class Course(var title: String? = null, var author: String? = null, var desc: String? = null){

}
open class FullCourse(title: String?, author: String?, desc: String?, var id: Int, var img: Bitmap) :
    Course(title, author, desc)
