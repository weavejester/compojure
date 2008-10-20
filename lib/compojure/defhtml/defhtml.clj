(ns compojure.defhtml
    (:use (compojure
	   html
	   validation)))

(defmacro defhtml [name letkeys & body]
  `(def ~name {:html (fn [params#]
			(let [{:keys ~letkeys} params#]
			  (html ~@body)))}))

(defn render 
  ([html-struct options]
     (cond
      (options :validate) 
          (binding [compojure.validation/validation-errors (get-validation-errors html-struct (options :params))]
	    (render html-struct (dissoc options :validate)))
       true
       ((html-struct :html) (options :data)))))
