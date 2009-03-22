;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

;; compojure.crypto:
;;
;; Functions for encoding, hashing and encrypting data.

(ns compojure.crypto
  (:use compojure.str-utils)
  (:use clojure.contrib.duck-streams)
  (:import java.net.URLEncoder)
  (:import java.net.URLDecoder))

(defn urlencode
  "Encode a urlencoded string using the default encoding."
  [s]
  (URLEncoder/encode (str* s) *default-encoding*))

(defn urldecode
  "Decode a urlencoded string using the default encoding."
  [s]
  (URLDecoder/decode s *default-encoding*))
