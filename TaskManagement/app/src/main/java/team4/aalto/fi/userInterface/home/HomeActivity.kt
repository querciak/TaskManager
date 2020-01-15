package team4.aalto.fi.userInterface.home

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.ProjectRepository
import team4.aalto.fi.dataInterface.HomeView
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.domain.usecase.UserProjectsUseCase
import team4.aalto.fi.userInterface.createProject.CreateProjectActivity
import team4.aalto.fi.userInterface.login.LoginActivity
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat
import ir.mirrajabi.searchdialog.core.SearchResultListener
import team4.aalto.fi.domain.model.SearchModel
import team4.aalto.fi.userInterface.profileSettings.SettingsActivity
import team4.aalto.fi.userInterface.projectDetails.ProjectDetailsActivity
import team4.aalto.fi.userInterface.selectProjectMember.SelectProjectMemberActivity

class HomeActivity: AppCompatActivity(), HomeView {

    private lateinit var presenter: HomePresenter
    private lateinit var userProjectsAdapter: UserProjectsAdapter
    private lateinit var currentUser: String
    var tab = 0
    private lateinit var  profileImage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //get username of current user
        currentUser = intent.getStringExtra("currentUser")

        //initialize presenter
        presenter = HomePresenter(this, UserProjectsUseCase(ProjectRepository()))
        presenter.gimmeTheImage(currentUser)

        tabOptions.selectedItemId = R.id.nav_dashboard
        tab = 1
        listOfProjects.layoutManager = LinearLayoutManager(this)
        userProjectsAdapter = UserProjectsAdapter(presenter, currentUser)
        listOfProjects.adapter = userProjectsAdapter

        //change tab (projects listed in different ways)
        tabOptions.setOnNavigationItemSelectedListener {
            presenter.manageList(it, currentUser)
        }

        //add new project
        addProjectButton.setOnClickListener {
            val intent = Intent(this, CreateProjectActivity::class.java)
            intent.putExtra("currentUser", currentUser)
            startActivity(intent)
        }

        //oper more options
        homeMenu.setOnClickListener {
            /** it shows dropdown menu and call homeMenuOptions from the presenter */
            val popup = PopupMenu(this, homeMenu)
            popup.menuInflater.inflate(R.menu.home_menu_profile_settin_and_logout,popup.menu)
            popup.setOnMenuItemClickListener {
                presenter.homeMenuOptions(it)
            }
            popup.show()
        }

        home_search.setOnClickListener {
            presenter.filterProjects(searchText.text.toString())
        }

        /** Get user from the project */
        //presenter.getUserProjects(currentUser)
    }

    override fun onResume() {
        super.onResume()
        if(tabOptions.selectedItemId != R.id.nav_dashboard) tabOptions.selectedItemId = R.id.nav_dashboard
        else {presenter.clearList()
        println("[HomeActivity] --------------------------------------- after clearList()")
        presenter.getUserProjects(currentUser)}
    }

    override fun resetView() {
        super.resetView()
        finish()
        overridePendingTransition(0,0)
        startActivity(getIntent())
        overridePendingTransition(0,0)
    }

    override fun openMemberPickerActivity(project: Project) {
        super.openMemberPickerActivity(project)
        val intent = Intent(this, SelectProjectMemberActivity::class.java)
        intent.putExtra("id", project.id)
        intent.putExtra("currentuser", currentUser)
        intent.putExtra("assigned_to", project.assigned_to)
        startActivity(intent)
    }

    override fun goToProjectDetails(project: Project) {
        super.goToProjectDetails(project)

        val intent = Intent(this, ProjectDetailsActivity::class.java)
        println(project.id + "---------------------------------------------------------------------------------")
        intent.putExtra("name",project.name)
        intent.putExtra("id", project.id)
        intent.putExtra("members", project.assigned_to)
        intent.putExtra("admin", project.project_admin)
        intent.putExtra("currentUser", currentUser)
        intent.putExtra("group", project.group_project)

        //itemView.context.startActivity(intent)
        startActivity(intent)
    }

    fun showCreateCategoryDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search Project")
        // https://stackoverflow.com/questions/10695103/creating-custom-alertdialog-what-is-the-root-view
        // Inflate view with null rootView
        val view = layoutInflater.inflate(R.layout.search_layout, null)
        builder.setView(view)
        // set up the search button
        builder.setPositiveButton("Search") { dialog, p1 ->
            /** Do something */
            dialog.dismiss()
        }
        //set up the cancel button
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }
        //show alert dialog
        builder.show()
    }

    // super cool function to create a dialog to search
    private fun searchFunction(){
        SimpleSearchDialogCompat(this, "Search", "Search Project..", null,
            initData(), SearchResultListener{baseSearchDialogCompat, item, position ->
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                baseSearchDialogCompat.dismiss()
            }).show()
    }
    private fun initData(): ArrayList<SearchModel> {
        val items = ArrayList<SearchModel>()

        items.add(SearchModel("Project 1"))
        items.add(SearchModel("Project 2"))
        items.add(SearchModel("Project z"))
        items.add(SearchModel("Project X"))

        return items
    }

    override fun hereYouHave(url: String) {
        super.hereYouHave(url)
        profileImage = url
    }

    override fun goToSettings() {
        super.goToSettings()
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("currentUser", currentUser)
        intent.putExtra("url",profileImage)
        startActivity(intent)
    }


    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    override fun logoutCurrentUser() {
        super.logoutCurrentUser()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun showLoading() {
        super.showLoading()
    }

    override fun hideLoading() {
        super.hideLoading()
    }

    override fun showProjects(projects: List<Project>) {
        super.showProjects(projects)
        runOnUiThread{
            println("[HomeActivity] print after start of the function (runOnUI)------- size : ${projects.size}")
            userProjectsAdapter.fillList(projects)
        }
    }

    override fun clearProjectList() {
        super.clearProjectList()
        userProjectsAdapter.clearList()
    }

    override fun checkCurrentTab()= tabOptions.selectedItemId

    override fun updateList(projectList: List<Project>) {
        super.updateList(projectList)
        runOnUiThread {
            userProjectsAdapter.clearList()
            userProjectsAdapter.fillList(projectList)
        }
    }

}

