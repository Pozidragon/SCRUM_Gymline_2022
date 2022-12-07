package com.example.gymline

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymline.databinding.ActivityCoursesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.io.File
import java.util.*


class CoursesActivity : AppCompatActivity() {
    lateinit var binding : ActivityCoursesBinding
    lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var dialog: Dialog

    private val adapter = CourseAdapter()

    private var index = 0

    private lateinit var tempItemCourse : View

    private lateinit var panel : SlidingUpPanelLayout

    private var isItemClickedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        panel = binding.slidingLayout

        panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN

        panel.isTouchEnabled = false

        auth = Firebase.auth

        init()

        binding.btmMenu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.profile_item -> {
                    if (!InternetConn.internetIsConnected()){
                        Toast.makeText(
                            baseContext, "No internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        val i = Intent(this, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                }
                R.id.logout -> {
                    if (!InternetConn.internetIsConnected()){
                        Toast.makeText(
                            baseContext, "No internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        auth.signOut()
                        val i = Intent(this, signInActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                }
                R.id.courses -> {
                    Toast.makeText(this@CoursesActivity,
                        "Current page: Courses", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        Thread(Runnable {
            this@CoursesActivity.runOnUiThread(java.lang.Runnable {
                startTimer()
            })
        }).start()
    }
    private fun init(input: String?= null, searchFilter: String? = null){
        showProgressBar()
        binding.apply {
            rcView.layoutManager = LinearLayoutManager(this@CoursesActivity)
            rcView.adapter = adapter

            databaseReference = FirebaseDatabase.getInstance("https://gymline-33603-default-rtdb.europe-west1.firebasedatabase.app").getReference("Courses")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(snapshotError: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    children.forEach {
                        val course = it.getValue(Course::class.java)
                        val id = it.key!!.toInt()

                        fun zaebalsa(input: String? = null, searchFilter: String? = null){
                            if(searchFilter.toString().contains(input.toString())){
                                storageReference = FirebaseStorage.getInstance().reference.child("Courses/$id.jpg")
                                val localFile = File.createTempFile("tempImage", "jpg")

                                storageReference.getFile(localFile).addOnSuccessListener {

                                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)


                                    val fullCourse =
                                        FullCourse(course!!.title, course.author, course.desc, id, bitmap)

                                    adapter.addCourse(fullCourse)

                                    hideProgressBar()
                                }
                            }

                        }

                        if(searchFilter == null && input == null) zaebalsa()
                        if(searchFilter == "Title") zaebalsa(input!!.uppercase(), course!!.title.toString().uppercase())
                        if(searchFilter == "Author") zaebalsa(input!!.uppercase(), course!!.author.toString().uppercase())
                        if(searchFilter == "Tags") zaebalsa(input!!.uppercase(), course!!.desc.toString().uppercase())

                        hideProgressBar()

                    }

                }
            })

        }

        dialog.setOnKeyListener { arg0, keyCode, event ->
            if(binding.searchCourseInput.isFocused){
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    binding.searchCourseInput.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(binding.searchCourseInput.windowToken, 0)
                }

            }
            true
        }

        adapter.setOnItemClickListener(object: CourseAdapter.OnItemClickListener{
            override fun onItemClick(fullCourse: FullCourse, item: View) {

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager  //keyboard-hiding
                imm?.hideSoftInputFromWindow(binding.searchCourseInput.windowToken, 0)

                if(binding.searchCourseInput.isFocused){
                    binding.searchCourseInput.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager  //keyboard-hiding
                    imm?.hideSoftInputFromWindow(binding.searchCourseInput.windowToken, 0)
                    //panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                }
                if(binding.topContainerCW.isVisible){
                    binding.searchCloseBtn.callOnClick()
                }
                else{
                    panel.setDragView(binding.dragThing)

                    if(panel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN || panel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {

                        binding.openedCourseTitle.text = fullCourse.title
                        binding.openedCourseImg.setImageBitmap(fullCourse.img)
                        binding.openedCourseAuthor.text = fullCourse.author
                        binding.openedCourseDesc.text = fullCourse.desc

                        if(panel.panelState == SlidingUpPanelLayout.PanelState.HIDDEN && isItemClickedOnce)tempItemCourse.findViewById<CardView>(R.id.cardViewItem).setBackgroundColor(Color.parseColor("#ffffff"))
                        tempItemCourse = item
                        tempItemCourse.findViewById<CardView>(R.id.cardViewItem).setBackgroundColor(Color.parseColor("#4D718BC3"))
                        binding.scrViewCourse.fullScroll(ScrollView.FOCUS_UP);
                        panel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }
                    else {
                        tempItemCourse.findViewById<CardView>(R.id.cardViewItem).setBackgroundColor(Color.parseColor("#ffffff"))
                        panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN

                    }
                }
                isItemClickedOnce = true
            }

        })

        panel.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {

            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                if(previousState == SlidingUpPanelLayout.PanelState.EXPANDED && isItemClickedOnce) {
                    tempItemCourse.findViewById<CardView>(R.id.cardViewItem).setBackgroundColor(Color.parseColor("#ffffff"))
                }
            }

        })

        val spinner: Spinner = binding.spinner3
            // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.searchFilterArray,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        binding.searchOpenBtn.setOnClickListener {
            binding.topContainerCW.startAnimation(AnimationUtils.loadAnimation(this@CoursesActivity, R.anim.searchappear))
            binding.searchOpenBtn.visibility = View.GONE
            binding.topContainerCW.visibility = View.VISIBLE
            panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            stopTimer()
        }
        binding.searchCloseBtn.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager  //keyboard-hiding
            imm?.hideSoftInputFromWindow(binding.searchCourseInput.windowToken, 0)

            binding.topContainerCW.startAnimation(AnimationUtils.loadAnimation(this@CoursesActivity, R.anim.searchdisappear))
            binding.topContainerCW.visibility = View.GONE
            binding.searchOpenBtn.startAnimation(AnimationUtils.loadAnimation(this@CoursesActivity, R.anim.arrowdppear))
            binding.searchOpenBtn.visibility = View.VISIBLE
            if(isItemClickedOnce && this::tempItemCourse.isInitialized) {
                panel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
            }

        }

        binding.searchBtn.setOnClickListener {
            binding.apply {

                val input = searchCourseInput.text.toString()
                val searchInput = spinner3.selectedItem.toString()

                if(input != ""){
                    adapter.clear()

                    init(input, searchInput)

                    searchCloseBtn.callOnClick()
                    binding.removeFiltersBtn.visibility = View.VISIBLE

                    binding.resultedTitle.text = "Shown results for '$input'"
                }
                else{
                    Toast.makeText(this@CoursesActivity,
                        "Type some text for searching", Toast.LENGTH_SHORT).show()
                }


            }
        }

        binding.removeFiltersBtn.setOnClickListener {
            init()
            binding.removeFiltersBtn.startAnimation(AnimationUtils.loadAnimation(this@CoursesActivity, R.anim.searchdisappear))
            binding.removeFiltersBtn.visibility = View.GONE
            binding.searchCourseInput.text.clear()

            binding.resultedTitle.text = "Top Rated Courses:"
        }

        binding.searchCourseInput.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                binding.searchBtn.callOnClick()
                true
            } else false
        }

    }

    private var carousalTimer: Timer? = null
    private fun startTimer() {
        carousalTimer = Timer() // At this line a new Thread will be created
        carousalTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Thread(Runnable {
                    this@CoursesActivity.runOnUiThread(java.lang.Runnable {
                        binding.searchOpenBtn.startAnimation(AnimationUtils.loadAnimation(this@CoursesActivity, R.anim.arrowtydasyda))
                    })
                 }).start()
            }
        }, 0, 5 * 800) // delay
    }

    private fun stopTimer() {
        carousalTimer!!.cancel()
    }


    private fun showProgressBar(){
        dialog = Dialog(this@CoursesActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        if (!this@CoursesActivity.isFinishing) {
            dialog.show()
        }

    }

    private fun hideProgressBar(){
        dialog.dismiss()
    }
}