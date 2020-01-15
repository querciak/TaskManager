package team4.aalto.fi.userInterface.selectProjectMember

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_select_project_members.*
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.SelectProjectMemberView
import team4.aalto.fi.userInterface.home.UserProjectsAdapter

class SelectProjectMemberActivity: AppCompatActivity(), SelectProjectMemberView {

    private lateinit var presenter: SelectProjectMemberPresenter
    private lateinit var selectProjectMemberAdapter: SelectProjectMemberAdapter
    private lateinit var projectId: String
    private lateinit var currentUser: String
    private lateinit var membersList: ArrayList<String>

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_project_members)
        projectId = intent.getStringExtra("id")
        currentUser = intent.getStringExtra("currentuser")
        membersList = intent.getStringArrayListExtra("assigned_to")

        //initialize presenter
        presenter = SelectProjectMemberPresenter(this, projectId)



        member_recyclerview.layoutManager = LinearLayoutManager(this)
        selectProjectMemberAdapter = SelectProjectMemberAdapter(presenter, currentUser, membersList, requestQueue)
        member_recyclerview.adapter = selectProjectMemberAdapter

        search_members.setOnClickListener {
            presenter.researchMembers(memberName.text.toString())
        }

    }

    override fun updateList(matchingList: List<String>) {
        super.updateList(matchingList)
        runOnUiThread{
            member_recyclerview.adapter = selectProjectMemberAdapter
            selectProjectMemberAdapter.fillList(matchingList)
        }

    }

    override fun clearRecycler() {
        super.clearRecycler()
        member_recyclerview.adapter = null
       // selectProjectMemberAdapter.clearScreen()
    }

    override fun showError(s: String) {
        super.showError(s)
        memberName.error = s
    }

    override fun showToast(s: String) {
        super.showToast(s)
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}