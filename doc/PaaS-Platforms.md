In addition to running Compojure on standalone servers, you can run them on Java PaaS'. This page lists  Java PaaS' that support Compojure.

# CloudBees
[CloudBees](http://developer.cloudbees.com/bin/view/Main/) is a Java-based PaaS. Developers can run a full develop-to-deploy cycle on CloudBees using CloudBees DEV@cloud (which is Jenkins/CI-as-a-Service + forge) and/or CloudBees RUN@cloud which is a deployment PaaS.

## Getting Started with Compojure on CloudBees
The easiest way to get started is to use the CloudBees Clojure ClickStart which sets up source, Jenkins build and continuous deployment to the the PaaS (shown in the [Example Project on CloudBees](https://github.com/weavejester/compojure/wiki/Example-project-on-cloudbees) section on the wiki).

## Deploying manually to CloudBees
Use the lein-cloudbees plugin as per https://clojars.org/lein-cloudbees

    lein cloudbees deploy

More info [here](http://wiki.cloudbees.com/bin/view/RUN/Clojure).
### Prerequisites

You will need [Leiningen][1] 2 or above installed.

[1]: https://github.com/technomancy/leiningen

### Notes

This currently runs as a war in a container - 
of course CloudBees can run plain <a href="https://developer.cloudbees.com/bin/view/RUN/Java+Container">JVM apps</a> so that is possible if desired. 


