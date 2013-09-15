(ns ingred.api
  (:use [ring.util.response])
  (:require [cornerstone.config :as cfg]
            [ingred.store :as store]))

(defn index []
  (slurp "resources/public/index.html"))

(defn- url-and-name [recipes]
  (map (fn [recipe] {:name (:name recipe) :url (str "/recipes/" (:id recipe))}) recipes))

(defn list-recipes
  ([] (response (url-and-name (store/list-all))))
  ([letter] (response (url-and-name (store/by-letter letter)))))

(defn list-recipes-for [ingredient]
  (response (url-and-name (store/by-ingredient ingredient))))

(defn list-ingredients []
  (response (into #{} (map #(assoc % :url (str "/ingredients/" (:name %))) (store/list-ingredients)))))

(defn load-recipe [id]
  (response (store/load id)))

(defn config []
  (response (cfg/config)))
