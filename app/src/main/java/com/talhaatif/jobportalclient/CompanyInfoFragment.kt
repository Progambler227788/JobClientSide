package com.talhaatif.jobportalclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.talhaatif.jobportalclient.databinding.FragmentCompanyInfoBinding

class CompanyInfoFragment : Fragment() {
    private var _binding: FragmentCompanyInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val companyOverview = arguments?.getString("companyOverview") ?: "No company overview available."
        val missionVision = arguments?.getString("missionVision") ?: "No mission and vision available."
        val totalEmployees = arguments?.getString("totalEmployees") ?: "No employee data available."

        binding.tvCompanyOverview.text = companyOverview
        binding.tvMissionVision.text = missionVision
        binding.tvTotalEmployees.text = totalEmployees
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(companyOverview: String, missionVision: String, totalEmployees: String): CompanyInfoFragment {
            val fragment = CompanyInfoFragment()
            val args = Bundle()
            args.putString("companyOverview", companyOverview)
            args.putString("missionVision", missionVision)
            args.putString("totalEmployees", totalEmployees)
            fragment.arguments = args
            return fragment
        }
    }
}