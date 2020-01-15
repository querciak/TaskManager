// How to setup your environment
// https://firebase.google.com/docs/functions/get-started

// -----
// Run this command to deploy your functions:
// $ firebase deploy --only functions
// -----

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

const PDFDocument = require('pdfkit');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

// these are for image resizing
// const gcs = require('@google-cloud/storage')();
const gm = require('gm').subClass({imageMagick: true});
const path = require('path');
const os = require('os');
const fs = require('fs');
const sharp = require('sharp');
const mkdirp = require('mkdirp-promise');
const UUID = require("uuid-v4");


/**
 *
 * Triggers every hour
 * Looks for projects with deadlines shorter then 1 day and notifies every user in that project.
 * Looks for tasks with deadlines shorter then 1 day and notifies the associated user.
 * Requires an index to be created on the Tasks collections to allow for a collectionGroup query
 *
 */

// exports.notificationsOnProjectDeadline = functions.region('europe-west1').https.onRequest(async (req, res) => {

exports.notificationsOnProjectDeadline = functions.region('europe-west1').pubsub.schedule('0 */1 * * *')

// exports.notificationsOnProjectDeadline = functions.region('europe-west1').pubsub.schedule('*/1 * * * *') // this is every 1 minute (for demonstration)
  .timeZone('America/New_York') // Users can choose timezone - default is America/Los_Angeles
  .onRun((context) => {

    // var reply = [];

    var users_projectNotif = [];
    var users_taskNotif = [];

    var deviceTokens_project = [];
    var deviceTokens_task = [];

    // first we go through the projects and check their deadlines
    db.collection("Projects").get().then(snapshot => {
      snapshot.forEach(doc => {

        var deadline_string = doc.data().deadline_date

        if (deadline_string !== "none"){

          // we will create a Date object from the document data
          const [day, month, year] = deadline_string.split('/')

          // for some reason, the months index starts counting at 0
          var deadline = new Date(parseInt(year), parseInt(month)-1, parseInt(day))

          // subtract 1 day from the deadline
          var deadline_minus_one_day = new Date(deadline.getTime() - (1 * 24 * 60 * 60 * 1000));

          var now = new Date()

          if (deadline_minus_one_day < now){
            // get the list of users to notify
            users_projectNotif = users_projectNotif.concat(doc.data().assigned_to)
            // the admin also gets notified
            var project_admin = doc.data().project_admin
            users_projectNotif = users_projectNotif.concat(project_admin)
          }
        }

      });

      // we're using chained promises for the database accesses because we need to access different collections
      // this collectionGroup('Tasks') function requires the coreesponding index to exist
      return db.collectionGroup('Tasks').get();
    })
    .then(snapshot => {
      snapshot.forEach(task => {
        var deadline_string = task.data().deadline_date

        if (deadline_string !== "none" && deadline_string !== null){

          // we will create a Date object from the document data
          const [day, month, year] = deadline_string.split('/')

          // for some reason, the months index starts counting at 0
          var deadline = new Date(parseInt(year), parseInt(month)-1, parseInt(day))

          // subtract 1 day from the deadline
          var deadline_minus_one_day = new Date(deadline.getTime() - (1 * 24 * 60 * 60 * 1000));

          var now = new Date()

          if (deadline_minus_one_day < now){

            // get the list of users to notify
            users_taskNotif = users_taskNotif.concat(task.data().assigned_to)

          }
        }
      })

      // return the reference to the collection with the user tokens
      return db.collection("UserTokens").get();
    })
    .then( snapshot => {
      snapshot.forEach(doc => {

        // project tokens
        if (users_projectNotif.includes(doc.data().username)){
          deviceTokens_project = deviceTokens_project.concat(doc.data().tokens)
        }

        // task tokens
        if (users_taskNotif.includes(doc.data().username)){
          deviceTokens_task = deviceTokens_task.concat(doc.data().tokens)
        }
      });

      // Project notification details.
      const payload_project = {
        notification: {
          title: 'Task Management Notification',
          body: 'You have a project deadline in 1 day!'
        }
      };

      // Task notification details.
      const payload_task = {
        notification: {
          title: 'Task Management Notification',
          body: 'You have a task deadline in 1 day!'
        }
      };

      // Send notifications to all tokens.
      admin.messaging().sendToDevice(deviceTokens_project, payload_project)
      .then(response => {
          console.log('Successfully sent message:', response);
          return null
        })
        .catch(error => {
          console.log('Error sending message:', error);
        });

      admin.messaging().sendToDevice(deviceTokens_task, payload_task)
      .then(response => {
          console.log('Successfully sent message:', response);
          return null
        })
        .catch(error => {
          console.log('Error sending message:', error);
        });

      // res.send(reply)

      return null
    })
    .catch(reason => {
      console.error('There was an error', reason);
    })

});


/**
 *
 * Generate project report in the form of a pdf document
 * Arguments: Calling this function requires a 'project' query parameter
 * example: https://europe-west1-mcc-fall-2019-g04.cloudfunctions.net/generateProjectReport?project=iojYsrgtAFVdwz6ejC5r
 *
 */

exports.generateProjectReport = functions.region('europe-west1').https.onRequest((req, res) => {

    let filename = req.query.project;

    // Stripping special characters
    filename = encodeURIComponent(filename) + '.pdf';

    var doc = new PDFDocument();

    // Setting response to 'attachment' (download).
    // If you use 'inline' here it will automatically open the PDF
    res.setHeader('Content-disposition', 'attachment; filename="' + filename + '"');
    res.setHeader('Content-type', 'application/pdf');

    db.collection("Projects").doc(req.query.project).get().then(project => {

      doc.fontSize(25).text("Project name: " + project.data().name)

      doc.moveDown()

      doc.text("Member list:")
      project.data().assigned_to.forEach(member => {
          doc.text(member)
      })

      doc.moveDown()

      // doc.text("Events:")
      // project.data().events.forEach(event => {
      //     doc.text(event)
      // })

      return null
      // db.collectionGroup('Tasks').where('author', '==', myUserId).get();
    }).then(_ => {

      doc.pipe(res.status(200));
      doc.end();

      return null
    }).catch(reason => {
      console.error('There was an error', reason);
    })



    // // draw some text
    // doc.fontSize(25)
    //    .text('Here is some vector graphics...', 100, 80);
    //
    // // some vector graphics
    // doc.save()
    //    .moveTo(100, 150)
    //    .lineTo(100, 250)
    //    .lineTo(200, 250)
    //    .fill("#FF3300");
    //
    // doc.circle(280, 200, 50)
    //    .fill("#6600FF");
    //
    // // an SVG path
    // doc.scale(0.6)
    //    .translate(470, 130)
    //    .path('M 250,75 L 323,301 131,161 369,161 177,301 z')
    //    .fill('red', 'even-odd')
    //    .restore();
    //
    // // and some justified text wrapped into columns
    // doc.text('And here is some wrapped text...', 100, 300)
    //    .font('Times-Roman', 13)
    //    .moveDown()
    //    .text("... lorem ipsum would go here...", {
    //      width: 412,
    //      align: 'justify',
    //      indent: 20,
    //      columns: 2,
    //      height: 300,
    //      ellipsis: true
    //    });

});



// data:
// - image
// - name
// - resolution
// - project

// returns image url of the highest resolution saved
exports.uploadImage = functions.https.onRequest(async (req, res) => {

  const base64EncodedImageString = req.body.data.image.replace(/^data:image\/\w+;base64,/, '');
  const imageBuffer = new Buffer(base64EncodedImageString, 'base64');

  var resolutions = new Map();
  resolutions.set("low", [640,480]);
  resolutions.set("high", [1280,960]);
  resolutions.set("full", [null,null]);


  var resolutionsValues = new Map();
  resolutionsValues.set("low", 1);
  resolutionsValues.set("high", 2);
  resolutionsValues.set("full", 3);

  var resolVal = resolutionsValues.get(req.body.data.resolution.trim())

  var photoURL = null

  if (resolVal === 3){
    let uuid = UUID();
    const resol = resolutions.get("full")
    const resultImage = await sharp(imageBuffer)
        .resize(resol[0], resol[1])
        .toBuffer()
        .then(imgData => {
            console.log('image resize: ', imgData)
            return imgData
        })
        .catch(err => console.log(`there was an error ${err}`))
    const filename = `images/${req.body.data.project}/${req.body.data.name}_full`;
    const file = admin.storage().bucket().file(filename);
    await file.save(resultImage, { metadata: {
              contentType: 'image/jpeg',
              metadata: {
                firebaseStorageDownloadTokens: uuid
              }
            } });
    file.makePublic()
    if (photoURL === null)
      photoURL = await file.getSignedUrl({ action: 'read', expires: '03-09-2491' }).then(urls => urls[0]);
    resolVal -= 1
  }

  if (resolVal === 2){
    let uuid = UUID();
    const resol = resolutions.get("high")
    const resultImage = await sharp(imageBuffer)
        .resize(resol[0], resol[1])
        .toBuffer()
        .then(imgData => {
            console.log('image resize: ', imgData)
            return imgData
        })
        .catch(err => console.log(`there was an error ${err}`))
    const filename = `images/${req.body.data.project}/${req.body.data.name}_high`;
    const file = admin.storage().bucket().file(filename);
    await file.save(resultImage, { metadata: {
              contentType: 'image/jpeg',
              metadata: {
                firebaseStorageDownloadTokens: uuid
              }
            } });
    file.makePublic()
    if (photoURL === null)
      photoURL = await file.getSignedUrl({ action: 'read', expires: '03-09-2491' }).then(urls => urls[0]);
    resolVal -= 1
  }

  let uuid = UUID();
  const resol = resolutions.get("low")
  const resultImage = await sharp(imageBuffer)
      .resize(resol[0], resol[1])
      .toBuffer()
      .then(imgData => {
          console.log('image resize: ', imgData)
          return imgData
      })
      .catch(err => console.log(`there was an error ${err}`))
  const filename = `images/${req.body.data.project}/${req.body.data.name}_low`;
  const file = admin.storage().bucket().file(filename);
  await file.save(resultImage, { metadata: {
            contentType: 'image/jpeg',
            metadata: {
              firebaseStorageDownloadTokens: uuid
            }
          } });
  file.makePublic()

  if (photoURL === null)
    photoURL = await file.getSignedUrl({ action: 'read', expires: '03-09-2491' }).then(urls => urls[0]);
  res.send({"data": photoURL})
})

