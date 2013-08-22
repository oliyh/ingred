(ns ingred.scraper
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as enlive])
  (:import [java.net URL]))

(def recipe-root "http://www.bbc.co.uk/food/recipes")

(defn- index-for [cuisine]
  (str "http://www.bbc.co.uk/food/recipes/search?keywords=&cuisines[]=" cuisine "&occasions[]=&chefs[]=&programmes[]="))

(defn- cuisine-name [a]
  (-> a enlive/html-resource (enlive/select [:span]) first :content first))

(defn cuisines []
  (map #(vector (cuisine-name %) (->  % :attrs :href))
       (-> "http://www.bbc.co.uk/food/cuisines" URL. enlive/html-resource
           (enlive/select [:ol#cuisines :li :h3 :a]))))
