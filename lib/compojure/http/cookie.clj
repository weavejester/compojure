

(defn- cookie-to-map [#^javax.servlet.http.Cookie cookie]
  "returns a clojure map out of the cookie's attributes"
  {:name (. cookie (getName))
   :value (. cookie (getValue))
   :path (. cookie (getPath))
   :comment (. cookie (getComment))
   :domain (. cookie (getDomain))
   :max_age (. cookie (getMaxAge))})

(defn get-cookies [request]
  (reduce 
   (fn [cookie-map cookie]
     (assoc cookie-map (cookie :name) cookie))
   {} (map (fn [cookie] 
	     (cookie-to-map cookie)) 
	   (. request (getCookies)))))

