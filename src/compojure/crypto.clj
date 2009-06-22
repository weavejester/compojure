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
  (:import java.security.SecureRandom)
  (:import javax.crypto.Cipher)
  (:import javax.crypto.KeyGenerator)
  (:import javax.crypto.Mac)
  (:import javax.crypto.spec.SecretKeySpec)
  (:import javax.crypto.spec.IvParameterSpec)
  (:import java.util.UUID))

(defn hmac
  "Generate a hashed message authentication code with the supplied key and
  algorithm on some string data."
  [key algorithm data]
  (let [spec  (SecretKeySpec. key algorithm)
        mac   (doto (Mac/getInstance algorithm)
                (.init spec))
        bytes (.doFinal mac (.getBytes data))]
    (base64-encode-bytes bytes)))

(defn gen-uuid
  "Generate a random UUID."
  []
  (str (UUID/randomUUID)))

(defn secure-random-bytes
  "Returns a random byte array of the specified size and algorithm.
   Defaults to SHA1PRNG."
  ([size] (secure-random-bytes size "SHA1PRNG"))
  ([size algorithm]
     (let [seed (make-array (. Byte TYPE) size)]
       (.nextBytes (SecureRandom/getInstance algorithm) seed)
       seed)))

(defn gen-iv-param
  "Generates a random IvParameterSpec for use with CBC encryption algorithms."
  [size]
  (IvParameterSpec. (secure-random-bytes size)))

(defn gen-key
  "Generates a SecretKey of the specified algorithm and size."
  [algorithm size]
  (let [key-gen (doto (KeyGenerator/getInstance algorithm)
                  (.init size))]
    (.generateKey key-gen)))

(defn- cipher
  "Clojure wrapper for using javax.crypto.Cipher on a byte array."
  [key algorithm params data mode]
  (let [cipher (doto (Cipher/getInstance algorithm)
                 (.init mode key params))]
    (.doFinal cipher data)))

(defn encrypt-bytes
  "Encrypts a byte array with the given key and algorithm."
  [key algorithm params data]
  (cipher key algorithm params data Cipher/ENCRYPT_MODE))

(defn decrypt-bytes
  "Decrypts a byte array with the given key and algorithm."
  [key algorithm params data]
  (cipher key algorithm params data Cipher/DECRYPT_MODE))

(defn encrypt
  "Base64 encodes and encrypts a string with the given key and algorithm."
  [key algorithm params s]
  (base64-encode-bytes (encrypt-bytes key algorithm params (.getBytes s))))

(defn decrypt
  "Base64 encodes and encrypts a string with the given key and algorithm."
  [key algorithm params s]
  (String. (decrypt-bytes key algorithm params (base64-decode-bytes s))))
