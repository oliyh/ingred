(ns ingred.api
  (:use [ring.util.response])
  (:require [cornerstone.config :as cfg]
            [ingred.store :as store]))

(defn index []
  (slurp "resources/public/index.html"))

(defn- id-and-name [recipes]
  (map #(select-keys % [:id :name]) recipes))

(defn list-recipes
  ([] (response (id-and-name (store/list-all))))
  ([letter] (response (id-and-name (store/by-letter letter)))))

(defn list-recipes-for [ingredient]
  (response (id-and-name (store/by-ingredient ingredient))))

(defn list-ingredients []
  (response (into #{} (map :name (store/list-ingredients)))))

(defn load-recipe [id]
  (response (store/load id)))

(defn config []
  (response (cfg/config)))
