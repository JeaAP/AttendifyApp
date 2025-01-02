package com.example.attendify

import android.icu.text.CaseMap.Title
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class ViewPagerAdapter : PagerAdapter() {

//    private lateinit var Title: Array<String>
//    private lateinit var Description: Array<String>

    private var images: Array<Int> = arrayOf(
        R.drawable.about1,
        R.drawable.about2,
        R.drawable.about3
    )

    private var titles: Array<String> = arrayOf(
        "Punctually",
        "Focus",
        "Self-Discipline"
    )

    private var descriptions: Array<String> = arrayOf(
        "Punctuality in attending school\nDemonstrates responsibility and discipline.",
        "Focus ensures that students excel both\n in punctuality and in their studies.",
        "Self-discipline helps students manage\n their time and excel in learning."
    )

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as View
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.slider_screen, container, false) as LinearLayout

        val ImageView = view.findViewById<ImageView>(R.id.sliderImage)
        val title = view.findViewById<TextView>(R.id.sliderTitle)
        val desc = view.findViewById<TextView>(R.id.sliderDesc)

        ImageView.setImageResource(images[position])
        title.text = titles[position]
        desc.text = descriptions[position]

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}