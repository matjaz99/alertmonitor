## Next release

* [CHANGE] What will be changed
* [FEATURE] New feature
* [ENHANCEMENT] Enhancement
* [BUGFIX] Bug fixing

## 1.0.1-SNAPSHOT / 2019-xx-xx

Release includes:

* [FEATURE] Pagination (#1)
* [FEATURE] Sorting by multiple columns (hold command key) (#2)
* [ENHANCEMENT] Changed base image from tomcat:8.5.37-jre8 to tomcat:8.5-jre8-alpine (because it's smaller)
* [BUGFIX] Minor bug fixes

## 1.0.0 / 2019-07-30

This is first release. Release includes:

* [FEATURE] Correlate alarm and clear pairs (active alerts)
* [FEATURE] Color alerts according to severity
* [FEATURE] GUI showing active alerts, journal (history) and requests in raw format
* [FEATURE] Recognize alertmanager alerts and process alerts in json format
* [FEATURE] Generic webhook - it accepts GET and POST requests in raw format (http headers, url parameters, body)
* [FEATURE] The application is written in Java and runs in Apache Tomcat with JSF 2.2 and Primefaces
