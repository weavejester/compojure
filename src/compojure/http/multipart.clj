;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.
;; Modified by Adam Blinkinsop <blinks@acm.org> in August 2009.

(ns compojure.http.multipart
  "Add multipart form handling to Compojure. Relies on the Apache Commons
   FileUpload library."
  (:use clojure.contrib.def)
  (:use compojure.map-utils)
  (:import [org.apache.commons.fileupload.servlet ServletFileUpload]))

(defn multipart?
  "Does this request contain multipart content?"
  [request]
  (ServletFileUpload/isMultipartContent request))

(defvar- upload (ServletFileUpload.))

(defn field-seq
  "Map field names to values, which will either be a simple string or map.

  Multipart values will be maps with content-type, name (original filename),
  and stream (an open input stream object)."
  [request]
  (map (fn [i] {(keyword (.getFieldName i))
                (if (.isFormField i)
                  (.getParameter request (.getFieldName i))
                  {:content-type (.getContentType i)
                   :name (.getName i)
                   :stream (.openStream i)})})
       (.getItemIterator upload request)))

(defn with-multipart
  [handler]
  (fn [request]
    (let [req (merge request {:params (merge (field-seq request))})]
      (handler request))))
