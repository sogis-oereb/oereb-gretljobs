// set default values
def gretlJobFilePath = '*'
def gretlJobFileName = 'build.gradle'
def jenkinsfileName = 'Jenkinsfile'
def jobPropertiesFileName = 'job.properties'

def jobNamePrefix = ''


// search for files (gretlJobFileName) that are causing the creation of a GRETL-Job
def jobFilePattern = "${gretlJobFilePath}/${gretlJobFileName}"
println 'job file pattern: ' + jobFilePattern
def jobFiles = new FileNameFinder().getFileNames(WORKSPACE, jobFilePattern)


// generate the jobs
println 'generating the jobs...'
for (jobFile in jobFiles) {
  // get the folder name (is at position 2 from the end of the jobFile path)
  def folderName = jobFile.split('/').getAt(-2)

  // define the job name
  def jobName = "${jobNamePrefix}${folderName}"
  println 'Job ' + jobName

  // set the path to the default Jenkinsfile
  def pipelineFilePath = "${WORKSPACE}/${jenkinsfileName}"
  // check if job provides its own Jenkinsfile
  def customPipelineFilePath = "${folderName}/${jenkinsfileName}"
  if (new File(WORKSPACE, customPipelineFilePath).exists()) {
    pipelineFilePath = customPipelineFilePath
    println '    custom pipeline file found: ' + customPipelineFilePath
  }
  // read Jenkinsfile content
  def pipelineScript = readFileFromWorkspace(pipelineFilePath)


  // set defaults for job properties
  def jobProperties = new Properties([
    'authorization.permissions':'nobody',
    'logRotator.numToKeep':'unlimited',
    'parameters.fileParam':'none',
    'parameters.stringParam':'buildDescription;Keine Beschreibung angegeben;Beschreibung/Grund für die Publikation der Daten',
    'triggers.upstream':'none',
    'triggers.cron':''
  ])
  def jobPropertiesFilePath = "${folderName}/${jobPropertiesFileName}"
  def jobPropertiesFile = new File(WORKSPACE, jobPropertiesFilePath)
  if (jobPropertiesFile.exists()) {
    println '    job properties file found: ' + jobPropertiesFilePath
    jobProperties.load(new FileReader(jobPropertiesFile))
  }

  def productionEnv = ("${PROJECT_NAME}" == 'agi-gretl-production')

  pipelineJob(jobName) {
    if (!productionEnv) { // we don't want the BRANCH parameter in production environment
      parameters {
        stringParam('BRANCH', 'main', 'Name of branch to check out')
      }
    }
    if (jobProperties.getProperty('parameters.fileParam') != 'none') {
      parameters {
        fileParam(jobProperties.getProperty('parameters.fileParam'), 'Select file to upload')
      }
    }
    if (jobProperties.getProperty('parameters.stringParam') != 'none') {
      def propertyValues = jobProperties.getProperty('parameters.stringParam').split(';')
      if (propertyValues.length == 3) {
        parameters {
          stringParam(propertyValues[0], propertyValues[1], propertyValues[2])
        }
      }
    }
    if (jobProperties.getProperty('authorization.permissions') != 'nobody') {
      authorization {
        permissions(jobProperties.getProperty('authorization.permissions'), ['hudson.model.Item.Build', 'hudson.model.Item.Read'])
      }
    }
    if (jobProperties.getProperty('logRotator.numToKeep') != 'unlimited') {
      logRotator {
        numToKeep(jobProperties.getProperty('logRotator.numToKeep') as Integer)
      }
    }
    if (jobProperties.getProperty('triggers.upstream') != 'none') {
      triggers {
        upstream(jobProperties.getProperty('triggers.upstream'), 'SUCCESS')
      }
    }
    if (jobProperties.getProperty('triggers.cron') != '' && productionEnv) { // we want triggers only in production environment
      triggers {
        cron(jobProperties.getProperty('triggers.cron'))
      }
    }

    environmentVariables {
      // make the Git repository URL available on the Jenkins agent
      env('GIT_REPO_URL', GRETL_JOB_REPO_URL_OEREB_V2)
    }

    definition {
      cps {
        script(pipelineScript)
        sandbox()
      }
    }
  }
}

// add a view (tab) for these jobs
listView('ÖREB-v2-GRETL-Jobs') {
  jobs {
    regex(/^(oerebv2_.*|gretl-job-generator-oerebv2)/)
  }
  columns {
    status()
    weather()
    name()
    lastSuccess()
    lastFailure()
    lastDuration()
    buildButton()
    favoriteColumn()
  }
}
