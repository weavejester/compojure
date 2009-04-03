;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.server.common
;;
;; Common functions for implementing compojure server wrapper.
;;
;; Current implementations define:
;;
;; (start server)
;; (stop server)
;; (defserver name)
;; (runserver {:optional options} path1 servlet1 pathn servletn)

(ns compojure.server.common
  (:import java.net.URL))

(defn get-host-and-path
  "Splits a path or URL into its hostname and path."
  [url-or-path]
  (if (re-find #"^\w+://" url-or-path)
    (let [url (URL. url-or-path)]
      [(.getHost url) (.getPath url)])
    [nil url-or-path]))

(defn apply-optional-map
  "If options is a map, calls (f options seq).  
   Otherwise, calls (f {} (cons options seq)).
   Can be used to define run-server."
  [f options & seq]
  (if (map? options)
    (apply f options seq)
    (apply f {} (cons options seq))))
