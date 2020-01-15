# CI/CD pipeline

### Overview

The CI/CD pipeline in this project is used for both continuous integration and continuous deployment. It is a standard Gitlab pipeline configured through the .gitlab-ci.yml file in the root directory of the project. All the additional files required by the pipeline are located in the ci-cd folder (also in the root directory).

For a brief summary of gitlab's pipelines see the section: [Gitlab pipeline structure](#gitlab-pipeline-structure)

apk's from the pipeline are available [here](https://drive.google.com/open?id=1482NVzK5lsH33SAtQEchHL7XrsddCbgY)

### Triggers

There are two ways to trigger a pipeline:

- push a commit
- create a tag or releases

Different jobs are executed depending on the pipeline trigger. A commit creates a debug apk, while a git release creates a release apk.

### Stages

The pipeline in this project has 4 different stages:

- **setup** :  where we build docker images. All of the jobs in the pipeline are executed in docker containers. The main factors in this design choice are increased portability, less server configuration and having the servers running jobs as soon as possible. Migrating servers and executing jobs with different runners is easy because the only thing that the server needs is to have a functional docker installation, in addition to gitlab-runner.

- **build** : build the application apk. The job executed depends on the pipeline trigger:
  - commit - build a debug apk
  - tag - build a release apk and sign it  


- **test** : run tests on the apk

- **deploy** : rename the apk and push it to a repository. The job executed depends on the pipeline trigger:
  - commit - the apk name includes a commit hash and commit time
  - tag - the apk name includes the tag name


### Additional information

##### Gitlab pipeline structure

A brief explanation of Gitlab's pipeline structure:

A pipeline consists of stages, and each stage can have one or more jobs associated with it. The job is then executed by a gitlab job runner on a server. There are also multiple types of runners available.

##### Infrastructure

The server used to execute the gitlab runners is a google compute server (n1-standard-1).

##### Other installation requirements

There are a few more necessary steps to have pipelines running:

- Environment variables.

There are a few variables used in the pipeline configuration file that need to be configured. The gitlab environment variables setting lets us avoid having to share sensitive information in public files.

- Runner registration

After installing gitlab-runner on a server, it is necessary to register that runner so that it can be used by a project.

- Cloud resources

For adequate running times, gradle requires a server with at least 4gb or ram. It is possible but not ideal to run a server with less memory.

