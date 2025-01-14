package com.example.attendify

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.attendify.databinding.ActivityNavigationBinding

class ActivityNavigation : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var slideViewPager: ViewPager
    private lateinit var dots: Array<TextView?>
    private lateinit var dotIndicator: LinearLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var skipButton: TextView
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inisialisasi ViewPager dan lainnya
                slideViewPager = binding.slideViewPager
        viewPagerAdapter = ViewPagerAdapter()
        slideViewPager.adapter = viewPagerAdapter

        // Inisialisasi LinearLayout untuk dot indicators
        dotIndicator = binding.dotIndicator
        setDotIndicator(0)  // Set initial dot indicators

        // Inisialisasi dan konfigurasi tombol
        skipButton = binding.skip
        nextButton = binding.next
        setupButtonListeners()

        // Listener untuk perubahan halaman pada ViewPager
        slideViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                setDotIndicator(position)
                if (position == 2) {
                    nextButton.text = "Get Started"
                } else {
                    nextButton.text = "Next"
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setupButtonListeners() {
        skipButton.setOnClickListener {
            val intent = Intent(this@ActivityNavigation, ActivityLogin::class.java)
            startActivity(intent)
            finish()
        }

        nextButton.setOnClickListener {
            val nextSlide = initView(1)
            if (nextSlide < viewPagerAdapter.count) {
                slideViewPager.currentItem = nextSlide
            } else {
                val intent = Intent(this@ActivityNavigation, ActivityLogin::class.java)
                startActivity(intent)
                finish()
            }
        }

        slideViewPager = binding.slideViewPager
        dotIndicator = binding.dotIndicator

        viewPagerAdapter = ViewPagerAdapter()
        slideViewPager.adapter = viewPagerAdapter

        setDotIndicator(0)
    }

    fun setDotIndicator(position: Int) {
        dots = arrayOfNulls(3)
        dotIndicator.removeAllViews()

        for (i in dots.indices) {
            dots[i] = TextView(this).apply {
                text = Html.fromHtml("&#8226;")
                textSize = 35f
                setTextColor(if (i == position) resources.getColor(R.color.OxfordBlue, theme) else resources.getColor(R.color.grey, theme))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(10, 0, 10, 0) // Margin antara dots
                }
            }
            dotIndicator.addView(dots[i])
        }
    }

    private fun initView(increment: Int): Int {
        return slideViewPager.currentItem + increment
    }
}