package team4.aalto.fi.userInterface.projectDetails

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import team4.aalto.fi.R
import team4.aalto.fi.userInterface.projectDetails.files.FilesFragment
import team4.aalto.fi.userInterface.projectDetails.images.ImagesFragment
import team4.aalto.fi.userInterface.projectDetails.tasks.TasksFragment


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context,
                           fm: FragmentManager,
                           private val currentUser: String,
                           private val projectId: String,
                           private val pMembers: ArrayList<String>,
                           private val adm: String,
                           private val g: Boolean,
                           private val pName: String): FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val TAB_TITLES = arrayOf(
        R.string.tab_text_tasks,
        R.string.tab_text_pictures,
        R.string.tab_text_files
    )

    lateinit var fragment: Fragment

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        val bundle = Bundle()
        bundle.putString("user", currentUser)
        bundle.putString("projectId", projectId)
        bundle.putStringArrayList("members", pMembers)
        bundle.putString("admin", adm)
        bundle.putBoolean("group", g)
        bundle.putString("projectName", pName)


        when(position){
            0 -> {
                //fragment = TasksFragment()
                //this way you can pass parameters
                fragment = TasksFragment.newInstance("tasks", "fragment")
                fragment.arguments = bundle
            }
            1 -> {
                fragment = ImagesFragment.newInstance("images", "fragment")
                fragment.arguments = bundle
            }
            2 -> {
                fragment = FilesFragment.newInstance("files", "fragment")
                fragment.arguments = bundle
            }
        }
        return fragment

    }

    override fun getPageTitle(position: Int): CharSequence? {

        when(position){
            0 -> {
                return "Tasks"
            }
            1 -> {
                return "Images"
            }
            2 -> {
                return "Files"
            }
        }
        return null
    }

    override fun getCount(): Int {
        // Show 3 total pages, from TAB_TITLES
        return TAB_TITLES.size
    }
}