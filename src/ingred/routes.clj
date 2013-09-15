(ns ingred.routes
  (:require [compojure.core
             :as c-core
             :refer [defroutes GET POST PUT DELETE HEAD OPTIONS PATCH ANY]]
            [compojure.route :as c-route]
            ;; Public APIs
            [ingred.api :as api]))

(defroutes site
  (GET "/" [] (api/index))

  (GET "/recipes/" [] (api/list-recipes))
  (GET "/recipes/:letter/" [letter] (api/list-recipes letter))
  (GET "/recipes/:id" [id] (api/load-recipe id))

  (GET "/ingredients/" [] (api/list-ingredients))
  (GET "/ingredients/:ingredient" [ingredient] (api/list-recipes-for ingredient))

  (GET "/config" [] (api/config)))

(defroutes app-routes
  (c-route/resources "/")
  (c-route/not-found "404 Page not found."))

(def all-routes
  (c-core/routes site app-routes))
