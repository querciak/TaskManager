package team4.aalto.fi.data.repository

import team4.aalto.fi.domain.model.Task
import team4.aalto.fi.domain.model.TaskStatus
import team4.aalto.fi.domain.model.User

// DUMMY CLASS for testing

class TaskRepository {
    companion object{
        fun createDataSet(): ArrayList<Task>{
            var mylist = arrayListOf<Task>()

/*
           // var users: ArrayList<User> = arrayListOf()
           // users.add(user1)
            var users: ArrayList<String> = arrayListOf()
            users.add("Andrea")

            var events: ArrayList<TaskStatus> = arrayListOf()
            events.add(TaskStatus("on-going","01/01/2019"))

            mylist.add(
                Task(
                    "task 1",
                    "today",
                    "tomorrow",
                     events,
                    "on-going",
                    users
                )
            )

            mylist.add(
                Task(
                    "task 2",
                    "today",
                    "tomorrow",
                     events,
                    "completed",
                    users
                )
            )

            mylist.add(
                Task(
                    "task 3",
                    "today",
                    "tomorrow",
                     events,
                    "completed",
                    users
                )
            )

            mylist.add(
                Task(
                    "task 4",
                    "today",
                    "tomorrow",
                     events,
                    "pending",
                    users
                )
            )
 */
            return mylist
        }


    }
}