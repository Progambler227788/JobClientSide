package com.talhaatif.jobportalclient.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.talhaatif.jobportalclient.CompanyInfoFragment
import com.talhaatif.jobportalclient.JobDescriptionFragment

class ViewPagerAdapter(fm: FragmentManager, private val jobDescription: String, private val companyOverview: String, private val missionVision: String, private val totalEmployees: String) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> JobDescriptionFragment.newInstance(jobDescription)
            1 -> CompanyInfoFragment.newInstance(companyOverview, missionVision, totalEmployees)
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }

    override fun getCount(): Int {
        return 2 // We have two fragments (Job Description and Company Info)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Job Description"
            1 -> "Company Info"
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}
