(ns ingred.api
  (:use [ring.util.response])
  (:require [cornerstone.config :as cfg]
            [ingred.store :as store]))

(defn index []
  (slurp "resources/public/index.html"))

(defn list-recipes []
  (map #(select-keys % [:id :name]) (store/list)))

(defn config []
  (response (cfg/config)))
