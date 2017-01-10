@ECHO ON

set svc_username=%1
set svc_password=%2

SET JAVA_HOME=${vars['remote.jwala.java.home']}
SET CATALINA_HOME=${vars['remote.paths.instances']}\\${jvm.jvmName}\\${vars['remote.tomcat.dir.name']}
SET TOMCAT_BIN_DIR=%CATALINA_HOME%\bin

if exist %TOMCAT_BIN_DIR%\setenv.bat CALL %TOMCAT_BIN_DIR%\setenv.bat
IF "%ERRORLEVEL%" NEQ "0" (
    EXIT %ERRORLEVEL%"
)

ECHO Run pre_install.bat
if exist %TOMCAT_BIN_DIR%\pre_install.bat call %TOMCAT_BIN_DIR%\pre_install.bat
IF "%ERRORLEVEL%" NEQ "0" (
    EXIT %ERRORLEVEL%"
)

ECHO Install the service
CMD /C %TOMCAT_BIN_DIR%\install-service.bat install ${jvm.jvmName}

SET SERVICE_OPTS=""
IF "%JAVA_SERVICE_OPTS%" NEQ "" SERVICE_OPTS="%SERVICE_OPTS% ++JvmOptions %JAVA_SERVICE_OPTS%"
IF "%START_PATH%" NEQ "" SERVICE_OPTS="%SERVICE_OPTS% --StartPath %START_PATH%"

ECHO Update Java Options
CMD /C  %TOMCAT_BIN_DIR%\tomcat7 //US//${jvm.jvmName} --JavaHome ${vars['remote.jwala.java.home']} %SERVICE_OPTS% --StdOutput "" --StdError ""
``
ECHO Change the service to automatically start
SC CONFIG ${jvm.jvmName} start= auto

ECHO Run post_install.bat
if exist %TOMCAT_BIN_DIR%\post_install.bat call %TOMCAT_BIN_DIR%\post_install.bat
IF "%ERRORLEVEL%" NEQ "0" (
    EXIT %ERRORLEVEL%"
)

if %svc_username%=="" goto :no_user

SC CONFIG ${jvm.jvmName} obj=%svc_username% password=%svc_password%

:no_user

EXIT %ERRORLEVEL%