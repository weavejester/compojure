;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns compojure.validation.predicates
    (:use compojure.str-utils))

(defn present?
  "True if x is not nil and not an empty string."
  [x]
  (not (blank? x)))

(defn max-size
  "Returns a function to check a maximum size of a collection."
  [n]
  #(<= (count %) n))
