(ns ingred.api
  (:use [ring.util.response])
  (:require [cornerstone.config :as cfg]
            [ingred.store :as store]
            [ingred.scraper :as scraper]))

(defn index []
  (slurp "resources/public/index.html"))

(defn- recipe-summary [recipes]
  (map (fn [recipe] {:name (:name recipe) :url (str "/recipes/" (:id recipe))}) recipes))

(defn list-recipes
  ([] (response (recipe-summary (store/list-all))))
  ([letter] (response (recipe-summary (store/by-letter letter)))))

(defn list-recipes-for [ingredient]
  (response (recipe-summary (store/by-ingredient ingredient))))

(defn list-ingredients []
  (response (into #{} (map #(assoc % :url (str "/ingredients/" (:name %))) (store/list-ingredients)))))

(defn load-recipe [id]
  (response (store/load-recipe id)))

(defn config []
  (response (cfg/config)))

(defn populate [letter]
  (response (scraper/scrape-all [letter])))

(defn wipe-store []
  (store/wipe-db)
  (response {:result "ok"}))
