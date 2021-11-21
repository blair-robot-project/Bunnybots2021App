package team449.frc.scoutingappbase.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import team449.frc.scoutingappbase.R
import team449.frc.scoutingappbase.adapters.MatchPagerAdapter
import team449.frc.scoutingappbase.databinding.FragmentMatchContainerBinding
import team449.frc.scoutingappbase.fragment.baseFragment.VMBaseFragment
import team449.frc.scoutingappbase.main.MainActivity
import kotlin.math.max
import kotlin.math.min

interface MatchContainerFragmentClickHandler {
    fun next(view: View)
    fun prev(view: View)
}

interface PageChanger {
    fun changePage(page: Int)
}

class MatchContainerFragment : VMBaseFragment<FragmentMatchContainerBinding>(), MatchContainerFragmentClickHandler, PageChanger {
    override val layoutId: Int = R.layout.fragment_match_container

    private lateinit var viewPagerAdapter: FragmentPagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)

        (activity as MainActivity).pageChanger = this
        binding.handler = this

        viewPagerAdapter = MatchPagerAdapter(childFragmentManager)
        viewPager.adapter = viewPagerAdapter
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageSelected(position: Int) { binding.page = position }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })

        val tabLayout = view.findViewById<TabLayout>(R.id.pageDots)
        tabLayout.setupWithViewPager(viewPager, true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideUpButton()
    }

    override fun changePage(page: Int) { viewPager.currentItem = page }
    override fun next(view: View) { changePage(min(viewPager.currentItem + 1, (viewPager.adapter?.count ?: 1) - 1)) }
    override fun prev(view: View) { changePage(max(viewPager.currentItem - 1, 0)) }
}
