;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.encodings
  "Functions for encoding data."
  (:use compojure.str-utils)
  (:use clojure.contrib.duck-streams)
  (:import java.net.URLEncoder)
  (:import java.net.URLDecoder)
  (:import org.apache.commons.codec.binary.Base64)
  (:import org.apache.commons.codec.binary.Hex))

(defn urlencode
  "Encode a urlencoded string using the default encoding."
  [s]
  (URLEncoder/encode (str* s) *default-encoding*))

(defn urldecode
  "Decode a urlencoded string using the default encoding."
  [s]
  (URLDecoder/decode s *default-encoding*))

(defn base64-encode-bytes
  "Encode an array of bytes into a base64 encoded string."
  [unencoded]
  (String. (Base64/encodeBase64 unencoded)))
 
(defn base64-encode
  [unencoded]
  "Encode a string using base64."
  (base64-encode-bytes (.getBytes unencoded)))
 
(defn base64-decode-bytes
  "Decode a string using base64 into an array of bytes."
  [encoded]
  (Base64/decodeBase64 (.getBytes encoded)))

(defn base64-decode
  "Decode a string using base64."
  [encoded]
  (String. (base64-decode-bytes encoded)))
  
(defn marshal
  "Serialize a Clojure object in a base64-encoded string."
  [data]
  (base64-encode (pr-str data)))
 
(defn unmarshal
  "Unserialize a Clojure object from a base64-encoded string."
  [marshaled]
  (read-string (base64-decode marshaled)))

(defn decode-hex
  "Converts a string of hex into it's corresponding byte array."
  [s]
  (Hex/decodeHex (.toCharArray s)))

(defn encode-hex
  "Converts a byte array into it's corresponding hex String."
  [array]
  (String. (Hex/encodeHex array)))
