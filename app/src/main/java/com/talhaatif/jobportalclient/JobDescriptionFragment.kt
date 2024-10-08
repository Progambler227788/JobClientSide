package com.talhaatif.jobportalclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.talhaatif.jobportalclient.databinding.FragmentJobDescriptionBinding

class JobDescriptionFragment : Fragment() {
    private var _binding: FragmentJobDescriptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jobDescription = arguments?.getString("jobDescription") ?: "No job description available."
        binding.tvJobDescription.text = jobDescription
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(jobDescription: String): JobDescriptionFragment {
            val fragment = JobDescriptionFragment()
            val args = Bundle()
            args.putString("jobDescription", jobDescription)
            fragment.arguments = args
            return fragment
        }
    }
}
