package com.talhaatif.jobportalclient.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.talhaatif.jobportalclient.ApplyJobsActivity
import com.talhaatif.jobportalclient.R
import com.talhaatif.jobportalclient.firebase.Variables
import com.talhaatif.jobportalclient.model.Job
import de.hdodenhof.circleimageview.CircleImageView

class JobAdapter(private var jobs: List<Job>, private val activity: Activity) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]

        holder.jobTitleTextView.text = job.jobTitle
        holder.companyAndLocationTextView.text = "${job.jobCompany}, ${job.jobLocation}"
        holder.jobTypeTextView.text = job.jobType
        holder.jobModeTextView.text = job.jobMode
        holder.salaryRangeTextView.text = "$${job.jobSalaryStartRange} - $${job.jobSalaryEndRange}"
        Glide.with(holder.jobImage.context)
            .load(job.jobImage)
            .placeholder(R.drawable.cartoon_happy_eyes)
            .into(holder.jobImage)
        // Fetch and set the job status
        Variables.db.collection("users").document(Variables.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val appliedJobs = document.get("appliedJobs") as? List<Map<String, Any>>
                val jobStatus = appliedJobs?.firstOrNull { (it["jid"] as? String) == job.jid }?.get("jobStatus") as? String ?: "Not Applied"
                holder.jobStatus.text = jobStatus

            }
            .addOnFailureListener {
                holder.jobStatus.text = "Not Applied"
            }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ApplyJobsActivity::class.java).apply {
                putExtra("jid", job.jid)
            }
            activity.startActivity(intent)
        }
    }

    fun updateJobs(newJobs: List<Job>) {
        this.jobs = newJobs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = jobs.size

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitleTextView: TextView = itemView.findViewById(R.id.job_title)
        val companyAndLocationTextView: TextView = itemView.findViewById(R.id.company_and_location)
        val jobTypeTextView: TextView = itemView.findViewById(R.id.job_type)
        val jobModeTextView: TextView = itemView.findViewById(R.id.job_mode)
        val salaryRangeTextView: TextView = itemView.findViewById(R.id.salary_range)
        val jobStatus : TextView = itemView.findViewById(R.id.status)
        val jobImage : CircleImageView = itemView.findViewById(R.id.company_logo)
    }
}
