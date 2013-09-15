(ns ingred.ring
  (:require [ingred.routes :as routes]
            [ring.middleware.json :as ring-json]
            [compojure.handler :as handler]))

(defn init []
  (println "Ingred is starting"))

(defn destroy []
  (println "Ingred is stopping"))

(def app routes/all-routes)

(defn get-handler [app]
  (-> (handler/api app)
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)))

(def war-handler (get-handler app))
