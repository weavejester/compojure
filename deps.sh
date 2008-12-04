#!/bin/sh
mkdir deps
cd deps

# Clojure
echo Downloading Clojure
svn co -q https://clojure.svn.sourceforge.net/svnroot/clojure clojure
echo Building clojure.jar
cd clojure/trunk
ant
mv clojure.jar ../..
cd ../..
rm -rf clojure

# Clojure-Contrib
echo Downloading Clojure-Contrib
svn co -q https://clojure-contrib.svn.sourceforge.net/svnroot/clojure-contrib clojure-contrib
echo Building clojure-contrib.jar
cd clojure-contrib/trunk
ant
mv clojure-contrib.jar ../..
cd ../..
rm -rf clojure-contrib

# Jetty
echo Downloading Jetty 6.1.14
wget http://dist.codehaus.org/jetty/jetty-6.1.14/jetty-6.1.14.zip
echo Extracting 
unzip -q jetty-6.1.14.zip
mv jetty-6.1.14/lib/jetty-6.1.14.jar .
mv jetty-6.1.14/lib/servlet-api-2.5-6.1.14.jar .
rm -rf jetty-6.1.14
rm jetty-6.1.14.zip
