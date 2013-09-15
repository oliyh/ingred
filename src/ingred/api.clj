(ns ingred.api
  (:use [ring.util.response])
  (:require [cornerstone.config :as cfg]))

(defn index []
  (slurp "resources/public/index.html"))

(defn config []
  (response (cfg/config)))
