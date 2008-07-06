(require "modules/http/http.clj")
(refer 'http)

(def *servlet* 
  (new-servlet
    (fn [context request response]
      (let [resource (find-resource request response)]
        (resource context request response)))))
