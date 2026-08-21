package com.example.zarur.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.zarur.R
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.OnboardingPage
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    @Inject
    lateinit var appPreferences: AppPreferences

    private val onboardingPages by lazy {
        listOf(
            OnboardingPage(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.onboarding1
            ),
            OnboardingPage(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.onboarding2
            ),
            OnboardingPage(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.onboarding3
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val btnNext = view.findViewById<View>(R.id.btnNext)
        val btnSkip = view.findViewById<View>(R.id.btnSkip)

        val adapter = OnboardingAdapter(onboardingPages)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingPages.size - 1) {
                viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        appPreferences.isFirstTimeLaunch = false
        findNavController().navigate(R.id.action_onboardingFragment_to_authHubFragment)
    }

    private inner class OnboardingAdapter(private val pages: List<OnboardingPage>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
            return OnboardingViewHolder(view)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(pages[position])
        }

        override fun getItemCount() = pages.size

        inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val ivOnboarding = view.findViewById<ImageView>(R.id.ivOnboarding)
            private val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            private val tvDescription = view.findViewById<TextView>(R.id.tvDescription)

            fun bind(page: OnboardingPage) {
                ivOnboarding.setImageResource(page.imageRes)
                tvTitle.text = page.title
                tvDescription.text = page.description
            }
        }
    }
}
