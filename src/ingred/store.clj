(ns ingred.store
  (:use monger.operators)
  (:require [monger.core :as m]
            [monger.collection :as mc]
            [monger.joda-time]
            [cornerstone.config :refer [config]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(def table "recipe")

(defn init
  "Connect to Mongo"
  []
  (m/connect-via-uri! (config :MONGOLAB_URI))
  (m/set-db! (m/get-db))) ;; db name is in the uri

(defn- replace-id [id m]
  (assoc (dissoc m :id) :_id id))

(defn save [m]
  (let [id (or (:id m) (ObjectId.))]
    (mc/update table {:_id id} (replace-id id m) :upsert true)
    {:id (str id)}))

(defn- replace-_id [m]
  (let [id (str (:_id m))]
    (assoc (dissoc m :_id) :id id)))

(defn load-recipe [id]
  (replace-_id (mc/find-map-by-id table (or (ObjectId/massageToObjectId id) id))))

(defn list-all []
  (map replace-_id (mc/find-maps table)))

(defn by-letter [letter]
  (map replace-_id (mc/find-maps table
                                 {:food.letter letter})))

(defn by-ingredient [ingredient]
  (map replace-_id (mc/find-maps table
                                 {:ingredients.name {$in [ingredient]}})))

(defn list-ingredients []
  (mapcat :ingredients (mc/find-maps table {} ["ingredients.name"])))

(defn search-recipes [term]
  (map replace-_id (mc/find-maps table
                                 {:name {$regex (str ".*" term ".*") $options "i"}})))

(defn wipe-db []
  (mc/remove table))
