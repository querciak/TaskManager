# Required Imports
import os
import json
from datetime import date
import logging
from flask import Flask, request, jsonify, logging as flog
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore, storage
logging.basicConfig(level=logging.INFO)
# Initialize Flask App
app = Flask(__name__)
port = int(os.environ.get('PORT', 8080))
# Initialize Firestore DB
cred = credentials.Certificate("service_key/key.json")
firebase_admin.initialize_app(cred, {
    'storageBucket': 'mcc-fall-2019-g04.appspot.com'
})


bucket = storage.bucket()

db = firestore.client()

project_ref = db.collection('Projects')
users_ref = db.collection('Users')


@app.route('/project', methods=['POST'])
def create():
    """
        create() : Add document to Firestore collection with request body
        e.g. json= {
                "name": "test2",
                "description": "my mcc project",
                "deadline" : "01/03/94",
                "group_project": true,
                "keyrwords": ["mcc","swe"],
                "project_admin": "Tmp",
                "project_img_url": "",
                "members": [],
                "tasks": [],
                "files_attached":[],
                "images_attached":[]
            }
    """
    try:
        doc_ref = project_ref.document()
        user_owner = request.json["project_admin"]
        doc_ref.set(request.json)
        users_document = users_ref.document(user_owner).collection("Projects").document()
        users_document.set({"reference": "Projects/"+doc_ref.id})

        return jsonify({"success": str(doc_ref.id)}), 200
    except Exception as e:
        return jsonify({'error': e}), 404

#  if not request.json or not 'title' in request.json
@app.route('/project', methods=['PUT'])
def update():
    """
        update() : Update document in Firestore collection with request body
        Ensure you pass a custom ID as part of json body in post request
        e.g. json={'id': 'gmhr8vU1cehf8ljX7zvx', 'members': ["agus","nicole","andrea"]}
    """
    try:
        id = request.json['id']
        new_members = request.json['members']
        project = project_ref.document(id).get().to_dict()
        current_members = project['members']
        for new_member in new_members:
            current_members.append(new_member)
            logging.info('current_members:', str(current_members))
        project_ref.document(id).update({"members": current_members})
        return jsonify({"success": True}), 200
    except Exception as e:
        return jsonify({'error': 'Not found'}), 404


@app.route('/project/<project_id>', methods=['DELETE'])
def delete(project_id):
    """
        delete() : Delete a document from Firestore collection
    """
    try:
        app.logger.info("default flask logging format")
        flog.default_handler.setFormatter(logging.Formatter("%(message)s"))
        app.logger.info(str(project_id))
        # Check for ID in URL query
        # tasks = project_ref.document(project_id).collection("Tasks").stream()
        project_reference = project_ref.document(project_id)
        project = project_reference.get().to_dict()
        current_tasks = project['tasks']
        current_files = project['files_attached']
        current_images = project['images_attached']
        app.logger.info(str(project))

        # app.logger.info("current_tasks:", str(current_tasks))

        if len(current_tasks) != 0:
            batch = db.batch()
            for task_id in current_tasks:
                task_reference = project_reference.collection("Tasks").document(task_id)
                batch.delete(task_reference)
                #   app.logger.info("-- TASK to be deleted:", str(task_id))

            batch.commit()
        if len(current_files) != 0:
            batch = db.batch()
            for file_id in current_files:
                #  app.logger.info("-- File to be deleted:", )
                #  app.logger.info(str(file_id))
                file_reference = project_reference.collection("Files").document(file_id)
                batch.delete(file_reference)
            batch.commit()

        if len(current_images) != 0:
            batch = db.batch()
            for image_id in current_images:
                #  app.logger.info("-- Image to be deleted:")
                #   app.logger.info(str(image_id))
                image_reference = project_reference.collection("Images").document(image_id)
                batch.delete(image_reference)
            batch.commit()
        project_ref.document(project_id).delete()

        # Remove reference from user
        # projects = db.collection_group(u'Projects') \
        #     .where(u'reference', u'==', u'Projects/'+project_id)
        # docs = projects.stream()
        # for doc in docs:
        #     print(u'{} => {}'.format(doc.id, doc.to_dict()))

        return jsonify({"success": True}), 200
    except Exception as e:
        return jsonify({'Error deleting Project': str(e)}), 404


def delete_collection(coll_ref, batch_size):
    docs = coll_ref.limit(batch_size).get()
    deleted = 0

    for doc in docs:
        print(u'Deleting doc {} => {}'.format(doc.id, doc.to_dict()))
        doc.reference.delete()
        deleted = deleted + 1

    if deleted >= batch_size:
        return delete_collection(coll_ref, batch_size)


@app.route('/task/<project_id>', methods=['POST'])
def createTask(project_id):
    """
    {
               "name": "Second task",
               "description": "Second project",
               "creation_date" : "01/03/1999",
               "deadline" : "01/03/2019",
               "status": "started",
               "assigned_to": ["agus","andrea"]
       }

    {
               "name": "Second task",
               "description": "Second project",
               "creation_date" : "01/03/1999",
               "deadline" : "01/03/2019",
               "status": "started",
               "events": [{"status": "started", "date":"01/03/1999"}],
               "assigned_to": ["agus","andrea"]
    }
    """
    try:
        task_ref = project_ref.document(project_id).collection(u'Tasks')
        #logging.info("'"SHOW:', str(project_id))
        doc_ref = task_ref.document()
        doc_ref.set(request.json)
        task_id = doc_ref.id
        #logging.info('TASK ID')
        #logging.info(str(task_id))
        # add task to a project list
        project_reference = project_ref.document(project_id)
        project = project_reference.get().to_dict()
        # logging.info('project')
        # logging.info(str(project))
        # add new taskId to project['tasks']
        current_tasks = project['tasks']
        current_tasks.append(task_id)
        # logging.info('current_tasks:')
        # logging.info(str(current_tasks))
        project_reference.update({"tasks": current_tasks})

        return jsonify({"success": task_id}), 200
    except Exception as e:
        return jsonify({'error': e}), 404


@app.route('/task/<task_id>', methods=['PUT'])
def update_status(task_id):
    """
        update_status() : Update document in Firestore collection with request body
        Ensure you pass a custom ID as part of json body in post request
        e.g. json={"proyectId": "gmhr8vU1cehf8ljX7zvx", "events": [{"status": "started", "date":"01/03/1999"}]
        send proyectId alongside task_id
    """
    try:
        logging.info("Update Task event!!:")
        logging.info(str(request.json))
        projectId = request.json["projectId"]
        # status = request.json["events"]
        project = project_ref.document(projectId).collection("Tasks").document(task_id).get().to_dict()
        current_events = project['events']
        logging.info("events in db:")
        logging.info(str(current_events))
       # create the data and event from here
        today = date.today()
        d1 = today.strftime("%d/%m/%Y")
        print("d1 =", d1)
        new_event = {"status": "completed", "date": d1}
        current_events.append(new_event)
        # for new_event in status:
        #     current_events.append(new_event)
        #     logging.info("Status_event:")
        #     logging.info(str(current_events))
        project_ref.document(projectId).collection("Tasks").document(task_id).update({"events": current_events})
        project_ref.document(projectId).collection("Tasks").document(task_id).update({"current_status": "completed"})
        return jsonify({"success": True}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 404


@app.route('/task/<project_id>/<task_id>', methods=['PUT'])
def update_task_members(project_id, task_id):
    """
        update() : Update document in Firestore collection with request body
        Ensure you pass a custom ID as part of json body in post request
        e.g. json={'id': 'gmhr8vU1cehf8ljX7zvx', 'members': ["agus","nicole","andrea"]}
    """
    try:
        new_members = request.json['assignedTo']
        task = project_ref.document(project_id).collection('Tasks').document(task_id).get().to_dict()
        current_members = task['assignedTo']
        for new_member in new_members:
            if  new_member not in current_members:
                current_members.append(new_member)
            logging.info("current_members:")
            logging.info(str(current_members))
        project_ref.document(project_id).collection('Tasks').document(task_id).update({"assignedTo": current_members})
        return jsonify({"success": True}), 200
    except Exception as e:
        return jsonify({'error': 'Not found'}), 404

# @app.route('/task/<project_id>/<task_id>', methods=['PUT'])
# def update(project_id,task_id):
#     """
#         update() : Update document in Firestore collection with request body
#         Ensure you pass a custom ID as part of json body in post request
#         e.g. json={'id': 'gmhr8vU1cehf8ljX7zvx', 'members': ["agus","nicole","andrea"]}
#     """
#     try:
#         id = request.json['id']
#         new_members = request.json['members']
#         project = project_ref.document(id).get().to_dict()
#         current_members = project['members']
#         for new_member in new_members:
#             current_members.append(new_member)
#             logging.info('current_members:', str(current_members))
#         project_ref.document(id).update({"members": current_members})
#         return jsonify({"success": True}), 200
#     except Exception as e:
#         return jsonify({'error': 'Not found'}), 404

# @app.route('/project', methods=['GET'])
# def listProjects():
#     logging.info('List projects', "get")
#     try:
#         snapshot = project_ref.stream()
#         for p in snapshot:
#             logging.info('project', str(p))
#         return jsonify({"success": True}), 200
#     except Exception as e:
#         return jsonify({'error': 'Not found'}), 404


@app.route('/project', methods=['GET'])
def listProjectsFiles():
    try:
       #bucket = storage.ref("Images/ProjectZ")
        bucket_ = storage.bucket('Images/')
       # logging.info('List projects')
       # bucket.listAll()
        return jsonify({"success": True}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 404



if __name__ == '__main__':
    app.run(threaded=True, host='0.0.0.0', port=port)