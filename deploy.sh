#!/bin/bash

cd TaskManagement
firebase deploy --token $(cat firebase-token)

cd ../backend
gcloud builds submit --config cloudbuild.yaml .
