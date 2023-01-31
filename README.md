# oereb-gretljobs

**Important note: This repository is not active anymore.
Development continues in https://github.com/sogis/oereb-gretljobs.**

Contains GRETL jobs for publishing data to OEREB-Kataster

## Job types

There are two different types of jobs in this repository:

* Jobs that allow for a review of the data
  before they are published to the *live* schema;
  all of these jobs are started manually only.
  A possible name for this kind of job might be "PLR topic jobs.
* Jobs that import data directly into the *stage* and *live* schema
  at the same time;
  some of these jobs are even started automatically every day.
  These jobs could be called "base data job" or similar.

### PLR topic jobs

This is the default job type.
It uses the [default Jenkinsfile](Jenkinsfile),
which first imports the data into the *stage* schema,
then asks the user for a review of the data,
and finally imports the data into the *live* schema.
Furthermore, the [GRETL Job generator script](gretl_job_generator.groovy)
makes sure for this job type that all job runs are being kept forever
(`'logRotator.numToKeep':'unlimited'`),
and that the user can add a description for each job run
(`'parameters.stringParam':'buildDescription;...'`).
Therefore, these jobs just need the `authorization.permissions` property
in their `job.properties` file.

### Base data jobs

These jobs need a special Jenkinsfile.
However, it's a much simpler one (e.g. [oerebv2_av/Jenkinsfile](oerebv2_av/Jenkinsfile)).
It needs to be placed directly in the job folder.

The job runs of these jobs don't need to be kept forever,
and they don't need a description.
In order to override the defaults,
they need the following properties in their `job.properties` file:

```
logRotator.numToKeep=15
parameters.stringParam=none
```

The jobs that automatically must run once a day
need the following additional property:

```
triggers.cron=H H(1-3) * * *
```

## Running a GRETL job locally using the GRETL wrapper script

For running a GRETL job using the GRETL wrapper script, see the `start-gretl.sh` command example below. Set any DB connection parameter environment variables before running the command.

If you want to set up development databases for developing new GRETL jobs, use the following `docker-compose` command before running your GRETL job in order to prepare the necessary DBs.

Start two DBs ("oereb" and "edit"), import data required for the data transformation (the so called legal basis data and some more files) into the "oereb" DB, and import demo data into the "edit" DB.

XTF files that are not available in this repo (Geotope, Denkmalschutz) contain not public data and are downloaded from a private S3 bucket.

Setup development environment for the various themes: see DEVELOP.md

Set environment variables containing the DB connection parameters and names of other resources:
```
export ORG_GRADLE_PROJECT_dbUriEdit="jdbc:postgresql://edit-db/edit"
export ORG_GRADLE_PROJECT_dbUserEdit="gretl"
export ORG_GRADLE_PROJECT_dbPwdEdit="gretl"
export ORG_GRADLE_PROJECT_dbUriOerebV2="jdbc:postgresql://oereb-db/oereb"
export ORG_GRADLE_PROJECT_dbUserOerebV2="gretl"
export ORG_GRADLE_PROJECT_dbPwdOerebV2="gretl"
export ORG_GRADLE_PROJECT_geoservicesHostName="geo-t.so.ch"
export ORG_GRADLE_PROJECT_gretlEnvironment="dev"
export ORG_GRADLE_PROJECT_awsAccessKeyAgi="xy"
export ORG_GRADLE_PROJECT_awsSecretAccessKeyAgi="yx"
export ORG_GRADLE_PROJECT_awsAccessKeyAda="foo"
export ORG_GRADLE_PROJECT_awsSecretAccessKeyAda="bar"
```



