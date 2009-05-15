;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.server.common
  "Common functions for implementing compojure server wrapper."
  (:import java.net.URL))

(defn get-host-and-path
  "Splits a path or URL into its hostname and path."
  [url-or-path]
  (if (re-find #"^[a-z+.-]+://" url-or-path)
    (let [url (URL. url-or-path)]
      [(.getHost url) (.getPath url)])
    [nil url-or-path]))

(defn server-with-options
  "Create a new server using the supplied function, options and servlets."
  [creator options servlets]
  (if (map? options)
    (creator options servlets)
    (creator {} (cons options servlets))))
