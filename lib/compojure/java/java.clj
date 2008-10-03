;; compojure.java
;; 
;; Java compatibility functions for Compojure.

(ns compojure.java
  (:use (compojure str-utils)))

(defn- method-names
  "Find the unique method names from a collection of methods."
  [coll]
  (set (map (memfn getName) coll)))

(defn methods
  "Return a set of all method names for the given class."
  [class]
  (method-names (.getMethods class)))

(defn declared-methods
  "Return a set of method names specifically declared by the given class."
  [class]
  (method-names (.getDeclaredMethods class)))

(defn getters
  "Find all the getter method names for a given class."
  [class]
  (method-names
    (filter #(and (.startsWith (.getName %) "get")
                  (= (count (.getParameterTypes %)) 0))
             (.getDeclaredMethods class))))

(defn setters
  "Find all the setter method names for a given class."
  [class]
  (method-names
    (filter #(and (.startsWith (.getName %) "set")
                  (= (count (.getParameterTypes %)) 1))
             (.getDeclaredMethods class))))

(defn invoke
  "Call a method on an object given the method name as a string."
  [object method & args]
  (eval `(. ~object (~(symbol method) ~@args))))

(defn- strip-get
  "Strip the initial 'get' off a getter and lowercase the new first letter."
  [method]
  (str (.toLowerCase (.substring method 3 4))
       (.substring method 4)))

(defn getmap
  "Turn all the getters of a Java object into a map."
  [object]
  (apply hash-map
    (mapcat (fn [m] [(keyword (strip-get m)) (invoke object m)])
            (getters (class object)))))

(defn prepend-set
  "Uppercase first letter and add 'set' to the beginning."
  [s]
  (str "set" (.toUpperCase (.substring s 0 1)) (.substring s 1)))

(defn setmap
  "Apply a setter on a Java object for each key/value pair in a map. If the key
  has no equivalent setter, it is ignored."
  [object map]
  (let [all-setters (setters (class object))]
    (doseq [key val] map
      (let [setter (prepend-set (str* key))]
        (if (contains? all-setters setter)
          (invoke object setter val))))
    object))
