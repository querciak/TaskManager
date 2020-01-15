# MCC Project Fall 2019 Group 4

### Folder Structure

- ci-cd: contains code relative to the **gitlab pipeline**
- TaskManagement: This is the name of the application we developed. This contains the code of the **android app**
- backend: This is the code with the implementation of the **api** and the files required to deploy it

### How to run

Run the gitlab pipeline to create an apk.

apk's are available [here](https://drive.google.com/open?id=1482NVzK5lsH33SAtQEchHL7XrsddCbgY)

You should use the latest apk in the staging folder.

To deploy the backend in Google Cloud Platform run the deploy.sh script


### Project Practicalities

For the development of the present project, the *Model View Presenter (MVP)* design pattern instead of the traditional *Model View Controller (MVC)* pattern.

The problem with the *MVC* approach is that all the logic is contained inside the controller activities
and the android activities become tightly-coupled to both UI and data access mechanisms. This probem for a small application is not a big deal. However, for a complex application it matters. As we are building up an application that uses Firebase as backend as a service in order to be able to easily scale up our application and can potentially become more complex, we decided to develop the application using the *MVP* to deal with some of the shortcomings of MVC. The *MVP* allowed us to improve the modularity of the application, which speeded up the development, as well as the data access mechanisms and the UI were more clean and easier to mantain.