(load-file "plugins/http/resource.clj")
(refer 'compojure-resource)

(defmacro GET "Creates a GET resource."
  [route & body]
  `(assoc-route "GET" ~route (new-resource ~@body)))

(defmacro PUT "Creates a PUT resource."
  [route & body]
  `(assoc-route "PUT" ~route (new-resource ~@body)))

(defmacro POST "Creates a POST resource."
  [route & body]
  `(assoc-route "POST" ~route (new-resource ~@body)))

(defmacro DELETE "Creates a DELETE resource."
  [route & body]
  `(assoc-route "DELETE" ~route (new-resource ~@body)))

(def *servlet* 
  (new-servlet
    (fn [context request response]
      (let [resource (find-resource request response)]
        (resource context request response)))))
