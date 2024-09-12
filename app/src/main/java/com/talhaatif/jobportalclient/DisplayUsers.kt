package com.talhaatif.jobportalclient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.talhaatif.jobportalclient.adapter.UserAdapter
import com.talhaatif.jobportalclient.databinding.FragmentDisplayUsersBinding
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.model.Job
import com.talhaatif.jobportalclient.model.User

class DisplayUsers : Fragment() {

    private  lateinit var  binding: FragmentDisplayUsersBinding
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var userApapter: UserAdapter
    private var userList =  mutableListOf<User>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDisplayUsersBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        shimmerFrameLayout = binding.shimmerViewContainer
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer() // Start shimmer animation

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isAdded && context!=null) {

            setupRecyclerView()
            loadUsersData()


        }
    }

    private fun setupRecyclerView() {
        userApapter = UserAdapter(emptyList(), requireActivity()) // Initialize with an empty list
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userApapter
        }
    }

    private fun loadUsersData() {


        Variables.db.collection("users") // firestore collection name is users
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // document jo json m ha usko convert kru User model class m
                    val user = document.toObject(User::class.java)
                    if (user.uid !=  (Variables.auth.currentUser?.uid ?: "") ){
                        userList.add(user)
                    }

                }
                userApapter.updateUsers(userList) // Update adapter with job list
                shimmerFrameLayout.stopShimmer() // Stop shimmer animation
                shimmerFrameLayout.visibility = View.GONE // Hide shimmer layout
                binding.rvUsers.visibility = View.VISIBLE // Show RecyclerView
            }
            .addOnFailureListener { e ->
                Variables.displayErrorMessage("Failed to load users: ${e.message}", requireContext())
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
            }
    }



}