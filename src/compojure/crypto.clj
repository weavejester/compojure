;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.crypto
  "Functions for cryptographically signing, verifying and encrypting data."
  (:use compojure.encodings)
  (:import javax.crypto.Mac)
  (:import javax.crypto.spec.SecretKeySpec)
  (:import java.util.UUID))

(defn hmac
  "Generate a hashed message authentication code with the supplied key and
  algorithm on some string data."
  [key algorithm data]
  (let [spec  (SecretKeySpec. (.getBytes key) algorithm)
        mac   (doto (Mac/getInstance algorithm)
                (.init spec))
        bytes (.doFinal mac (.getBytes data))]
    (base64-encode-bytes bytes)))

(defn gen-uuid
  "Generate a random UUID."
  []
  (str (UUID/randomUUID)))
