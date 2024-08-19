package com.talhaatif.jobportalclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.talhaatif.jobportalclient.adapter.JobAdapter
import com.talhaatif.jobportalclient.databinding.FragmentHomeBinding
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.model.Job

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var jobAdapter: JobAdapter
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        shimmerFrameLayout = binding.shimmerViewContainer
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer() // Start shimmer animation


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isAdded && context!=null) {

            setupRecyclerView()
            loadJobData()

            binding.searchBar.addTextChangedListener {
                currentQuery = it.toString().trim()
                val filteredList = mutableListOf<Job>()
                filteredList.clear()
                for( item in jobList){
                    if(currentQuery.uppercase() in item.jobCompany.uppercase()){
                        filteredList.add(item)
                    }
                }
                jobAdapter.updateJobs(filteredList)
            }

        }
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(emptyList(), requireActivity()) // Initialize with an empty list
        binding.rvJob.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jobAdapter
        }
    }
    private var jobList =  mutableListOf<Job>()

    private fun loadJobData() {


        Variables.db.collection("jobs")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val job = document.toObject(Job::class.java)
                    jobList.add(job)
                }
                jobAdapter.updateJobs(jobList) // Update adapter with job list
                shimmerFrameLayout.stopShimmer() // Stop shimmer animation
                shimmerFrameLayout.visibility = View.GONE // Hide shimmer layout
                binding.rvJob.visibility = View.VISIBLE // Show RecyclerView
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage("Failed to load jobs: ${e.message}", requireContext())
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
            }
    }
}
