(ns test.compojure.crypto
  (:import javax.crypto.spec.SecretKeySpec)
  (:import org.apache.commons.codec.binary.Hex)
  (:use compojure.crypto)
  (:use clojure.contrib.test-is))

(defn decode-hex
  [s]
  (Hex/decodeHex (.toCharArray s)))

(defn encode-hex
  [array]
  (String. (Hex/encodeHex array)))

(def *algorithm* "AES")
(def *key-size* 128)

(def *test-data*
  {128 {:key
        "95A8EE8E89979B9EFDCBC6EB9797528D"
        :input
        "4EC137A426DABF8AA0BEB8BC0C2B89D6"
        :padded-input
        "4EC137A426DABF8AA0BEB8BC0C2B89D6C6C9630F40AC1CCCCCAF67499CDF6971"
        :ciphered
        "D9B65D1232BA0199CDBD487B2A1FD646C6C9630F40AC1CCCCCAF67499CDF6971"
        :deciphered
        "9570C34363565B393503A001C0E23B65"}
   256 {:key
        "95A8EE8E89979B9EFDCBC6EB9797528D432DC26061553818EA635EC5D5A7727E"
        :input
        "4EC137A426DABF8AA0BEB8BC0C2B89D6"
        :padded-input
        "4EC137A426DABF8AA0BEB8BC0C2B89D6D21399E1AD5029A2EB9A94B3DD44405E"
        :ciphered
        "2F9CFDDBFFCDE6B9F37EF8E40D512CF4D21399E1AD5029A2EB9A94B3DD44405E"
        :deciphered
        "110A3545CE49B84BBB7B35236108FA6E"
        }})

(defn- test-data
  [k]
  (decode-hex ((*test-data* *key-size*) k)))

(def *secret-key* (SecretKeySpec. (test-data :key) "AES"))

(defn encrypt-test-data
  [k]
  (encrypt-bytes *secret-key* *algorithm* nil (test-data k)))

(defn decrypt-test-data
  [k]
  (decrypt-bytes *secret-key* *algorithm* nil (test-data k)))

(deftest test-encrypt-bytes
  (is (= (encode-hex (encrypt-test-data :input))
         (encode-hex (test-data :ciphered)))))

(deftest test-decrypt-bytes
  (is (= (encode-hex (decrypt-test-data :padded-input))
         (encode-hex (test-data :deciphered)))))

(deftest test-encrypt-decrypt
  (let [message "Foo Bar Bizz Buzz"
        algorithm "AES/CBC/PKCS5Padding"
        param (gen-iv-param 16)]
    (is (= (decrypt *secret-key* algorithm param
                    (encrypt *secret-key* algorithm param message))
           message))))
