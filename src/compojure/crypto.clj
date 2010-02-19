;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.crypto
  "Functions for cryptographically signing, verifying and encrypting data."
  (:use compojure.encodings
        clojure.contrib.def
        clojure.contrib.java-utils)
  (:import java.security.SecureRandom
           [javax.crypto Cipher KeyGenerator Mac]
           [javax.crypto.spec SecretKeySpec IvParameterSpec]
           java.util.UUID))

(defvar hmac-defaults
  {:algorithm "HmacSHA256"}
  "Default options for HMACs.")

(defvar encrypt-defaults
  {:algorithm  "AES"
   :key-size   128
   :mode       "CBC"
   :padding    "PKCS5Padding"}
  "Default options for symmetric encryption.")

(defn secure-random-bytes
  "Returns a random byte array of the specified size. Can optionally supply
   an PRNG algorithm (defaults is SHA1PRNG)."
  ([size]
    (secure-random-bytes size "SHA1PRNG"))
  ([size algorithm]
     (let [seed (make-array Byte/TYPE size)]
       (.nextBytes (SecureRandom/getInstance algorithm) seed)
       seed)))

(defn gen-secret-key
  "Generate a random secret key from a map of encryption options."
  ([]
    (gen-secret-key {}))
  ([options]
    (secure-random-bytes (/ (options :key-size) 8))))

(defn gen-uuid
  "Generate a random UUID."
  []
  (str (UUID/randomUUID)))

(defn- to-bytes
  "Converts its argument into an array of bytes."
  [x]
  (cond
    (string? x)     (.getBytes x)
    (sequential? x) (into-array Byte/TYPE x)
    :else           x))

(defn hmac-bytes
  "Generate a HMAC byte array with the supplied key on a byte array of data.
  Takes an optional map of cryptography options."
  [options key data]
  (let [options   (merge hmac-defaults options)
        algorithm (options :algorithm)
        hmac      (doto (Mac/getInstance algorithm)
                    (.init (SecretKeySpec. key algorithm)))]
    (.doFinal hmac data)))

(defn hmac
  "Generate a Basc64-encoded HMAC with the supplied key on a byte array or
  string of data. Takes an optional map of cryptography options."
  [options key data]
  (base64-encode-bytes (hmac-bytes options key (to-bytes data))))

(defn- make-algorithm
  "Return an algorithm string suitable for JCE from a map of options."
  [options]
  (str (options :algorithm) "/" (options :mode) "/" (options :padding)))

(defn- make-cipher
  "Create an AES Cipher instance."
  [options]
  (Cipher/getInstance (make-algorithm options)))

(defn encrypt-bytes
  "Encrypts a byte array with the given key and encryption options."
  [options key data]
  (let [options    (merge encrypt-defaults options)
        cipher     (make-cipher options)
        secret-key (SecretKeySpec. key (options :algorithm))
        iv         (secure-random-bytes (.getBlockSize cipher))]
    (.init cipher Cipher/ENCRYPT_MODE secret-key (IvParameterSpec. iv))
    (to-bytes (concat iv (.doFinal cipher data)))))

(defn decrypt-bytes
  "Decrypts a byte array with the given key and encryption options."
  [options key data]
  (let [options    (merge encrypt-defaults options)
        cipher     (make-cipher options)
        [iv data]  (split-at (.getBlockSize cipher) data)
        iv-spec    (IvParameterSpec. (to-bytes iv))
        secret-key (SecretKeySpec. key (options :algorithm))]
    (.init cipher Cipher/DECRYPT_MODE secret-key iv-spec)
    (.doFinal cipher (to-bytes data))))

(defn encrypt
  "Encrypts a string or byte array with the given key and encryption options."
  [options key data]
  (base64-encode-bytes (encrypt-bytes options key (to-bytes data))))

(defn decrypt
  "Base64 encodes and encrypts a string with the given key and algorithm."
  [options key data]
  (String. (decrypt-bytes options key (base64-decode-bytes data))))

(defn seal
  "Seal a data structure into a cryptographically secure string. Ensures no-one
  looks at or tampers with the data inside."
  [key data]
  (let [data (encrypt {} key (marshal data))]
    (str data "--" (hmac {} key data))))

(defn unseal
  "Read a cryptographically sealed data structure."
  [key data]
  (let [[data mac] (.split data "--")]
    (if (= mac (hmac {} key data))
      (unmarshal (decrypt {} key data)))))
