;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.response:
;;
;; Parse a Compojure route return value into a HTTP response map.

(ns compojure.http.response
  (:use clojure.contrib.def)
  (:import clojure.lang.IPersistentVector)
  (:import java.util.Map)
  (:import clojure.lang.ISeq)
  (:import java.io.File)
  (:import java.io.InputStream)
  (:import java.net.URL)
  (:import clojure.lang.Keyword))

(defmulti response-from class)

(defmethod response-from IPersistentVector
  [updates]
  (apply merge
    (map response-from updates)))

(defmethod response-from String
  [string]
  {:body string})

(defmethod response-from Map
  [headers]
  {:headers (into {} headers)})

(defmethod response-from Integer
  [status]
  {:status status})

(defmethod response-from ISeq
  [a-seq]
  {:body a-seq})

(defmethod response-from File
  [file]
  {:body file})

(defmethod response-from InputStream
  [stream]
  {:body stream})

(defmethod response-from URL
  [url]
  (response-from (.openStream url)))

(defmethod response-from Keyword
  [kw]
  (if (not= kw :next)
    (response-from (str kw))))

(defmethod response-from nil
  [_]
  {})

(defvar default-response
  {:status 200, :headers {}}
  "Default HTTP response map.")

(defn create-response
  "Create a new response map from a Clojure object, x."
  [x]
  (merge default-response (response-from x)))
