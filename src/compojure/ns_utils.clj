(ns compojure.ns-utils)

(defn- merge-meta!
  "Destructively merge metadata from a source object into a target."
  [source target]
  (.setMeta target
    (merge (meta source)
           (select-keys (meta target) [:name :ns]))))

(defn immigrate
  "Add all the public vars in a list of namespaces to the current namespace."
  [& namespaces]
  (doseq [ns namespaces]
    (require ns)
    (doseq [[sym v] (ns-publics (find-ns ns))]
      (merge-meta! v
        (if (.isBound v)
          (intern *ns* sym (var-get v))
          (intern *ns* sym))))))
