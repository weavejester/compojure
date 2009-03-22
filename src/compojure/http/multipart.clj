;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.http.multipart:
;;
;; Add multipart form handling to Compojure. Relies on the Apache Commons
;; FileUpload library.

(ns compojure.http.multipart
  (:use clojure.contrib.def)
  (:use compojure.map-utils)
  (:import org.apache.commons.fileupload.FileUpload)
  (:import org.apache.commons.fileupload.RequestContext)
  (:import org.apache.commons.fileupload.disk.DiskFileItemFactory)
  (:import org.apache.commons.fileupload.disk.DiskFileItem))

(defn multipart-form?
  "Does a request have a multipart form?"
  [request]
  (if-let [content-type (:content-type request)]
    (.startsWith content-type "multipart/form-data")))

(defvar- file-upload
  (FileUpload.
    (doto (DiskFileItemFactory.)
      (.setSizeThreshold -1)
      (.setFileCleaningTracker nil)))
  "Uploader class to save multipart form values to temporary files.")

(defn- request-context
  "Create a RequestContext object from a request map."
  [request]
  (proxy [RequestContext] []
    (getContentType []       (:content-type request))
    (getContentLength []     (:content-length request))
    (getCharacterEncoding [] (:character-encoding request))
    (getInputStream []       (:body request))))

(defn- file-map
  "Create a file map from a DiskFileItem."
  [#^DiskFileItem item]
  {:disk-file-item item
   :filename       (.getName item)
   :size           (.getSize item)
   :content-type   (.getContentType item)
   :tempfile       (.getStoreLocation item)})

(defn parse-multipart-params
  "Parse a map of multipart parameters from the request."
  [request]
  (reduce
    (fn [param-map, #^DiskFileItem item]
      (assoc-vec param-map
        (keyword (.getFieldName item))
        (if (.isFormField item)
          (.getString item)
          (file-map item))))
    {}
    (.parseRequest
       file-upload
       (request-context request))))

(defn get-multipart-params
  "Retrieve multipart params from the request."
  [request]
  (if (multipart-form? request)
    (parse-multipart-params request)
    {}))
